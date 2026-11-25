package com.example.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Testes do RNF03 no gateway: login, 401, 403, sobrescrita dos headers de identidade,
 * roteamento e CORS. Core e ai-service sao substituidos por servidores falsos que devolvem
 * os headers que receberam.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewaySegurancaTest {

    private static final ServicoFalso CORE = ServicoFalso.iniciar("core");
    private static final ServicoFalso IA = ServicoFalso.iniciar("ai-service");

    @DynamicPropertySource
    static void rotas(DynamicPropertyRegistry registry) {
        registry.add("CORE_URL", CORE::url);
        registry.add("AI_URL", IA::url);
    }

    @AfterAll
    static void pararServicos() {
        CORE.parar();
        IA.parar();
    }

    @Autowired
    private WebTestClient client;

    @Autowired
    private JwtEncoder jwtEncoder;

    // ---------- login ----------

    @Test
    @DisplayName("Login valido devolve token, login, roles e colaboradorId")
    void loginValidoDevolveToken() {
        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("login", "ana", "senha", "ana123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .jsonPath("$.login").isEqualTo("ana")
                .jsonPath("$.roles[0]").isEqualTo("COLABORADOR")
                .jsonPath("$.colaboradorId").isEqualTo(1);
    }

    @Test
    @DisplayName("Admin loga sem colaboradorId")
    void adminSemColaborador() {
        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("login", "admin", "senha", "admin123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.roles[0]").isEqualTo("ADMIN")
                .jsonPath("$.colaboradorId").doesNotExist();
    }

    @Test
    @DisplayName("Senha errada da 401 no formato {erro}")
    void senhaErradaDa401() {
        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("login", "ana", "senha", "errada"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.erro").isNotEmpty();
    }

    @Test
    @DisplayName("Login inexistente da 401")
    void loginInexistenteDa401() {
        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("login", "ninguem", "senha", "admin123"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Login sem corpo da 400")
    void loginSemCorpoDa400() {
        client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.erro").isNotEmpty();
    }

    // ---------- 401 ----------

    @Test
    @DisplayName("Sem token em /api/cursos da 401 no formato {erro}")
    void semTokenDa401() {
        client.get().uri("/api/cursos")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().jsonPath("$.erro").isNotEmpty();
    }

    @Test
    @DisplayName("Token adulterado da 401")
    void tokenAdulteradoDa401() {
        String token = login("ana", "ana123");
        String adulterado = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");
        client.get().uri("/api/cursos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adulterado)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Token expirado da 401")
    void tokenExpiradoDa401() {
        Instant passado = Instant.now().minusSeconds(3 * 3600);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gameeducation-gateway")
                .subject("ana")
                .issuedAt(passado)
                .expiresAt(passado.plusSeconds(3600))
                .claim("roles", List.of("COLABORADOR"))
                .claim("colaboradorId", 1L)
                .build();
        String expirado = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        client.get().uri("/api/cursos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expirado)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Token assinado com outra chave (admin forjado) da 401")
    void tokenComOutraChaveDa401() {
        byte[] outraChave = "chave-de-um-atacante-com-mais-de-32-bytes!!".getBytes(StandardCharsets.UTF_8);
        JwtEncoder atacante = NimbusJwtEncoder.withSecretKey(new SecretKeySpec(outraChave, "HmacSHA256"))
                .algorithm(MacAlgorithm.HS256).build();
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gameeducation-gateway").subject("admin")
                .issuedAt(agora).expiresAt(agora.plusSeconds(3600))
                .claim("roles", List.of("ADMIN")).build();
        String forjado = atacante.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        client.post().uri("/api/cursos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + forjado)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ---------- 403 ----------

    @Test
    @DisplayName("ana (COLABORADOR) em POST /api/cursos da 403 no formato {erro}")
    void colaboradorCriandoCursoDa403() {
        client.post().uri("/api/cursos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + login("ana", "ana123"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("titulo", "x"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody().jsonPath("$.erro").isNotEmpty();
    }

    @Test
    @DisplayName("Todas as rotas so de ADMIN dao 403 para COLABORADOR")
    void rotasDeAdminBloqueadasParaColaborador() {
        String token = login("carlos", "carlos123");
        client.post().uri("/api/colaboradores").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange().expectStatus().isForbidden();
        client.get().uri("/api/colaboradores").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange().expectStatus().isForbidden();
        client.post().uri("/api/treinamentos").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange().expectStatus().isForbidden();
        client.post().uri("/api/ranking/fechamentos/2026-11").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("COLABORADOR passa nas rotas que so exigem login (o Core decide o resto)")
    void colaboradorPassaEmRotaAutenticada() {
        String token = login("ana", "ana123");
        client.get().uri("/api/colaboradores/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.servico").isEqualTo("core");
        client.get().uri("/api/cursos?area=DEVOPS").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.path").isEqualTo("/api/cursos?area=DEVOPS");
    }

    @Test
    @DisplayName("ADMIN passa em POST /api/cursos")
    void adminCriaCurso() {
        client.post().uri("/api/cursos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + login("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("titulo", "x"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.servico").isEqualTo("core");
    }

    // ---------- headers de identidade ----------

    @Test
    @DisplayName("Anti-falsificacao: X-User-Roles: ADMIN mandado pela ana e sobrescrito")
    void headersForjadosSaoSobrescritos() {
        client.get().uri("/api/cursos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + login("ana", "ana123"))
                .header("X-User-Roles", "ADMIN")
                .header("X-User-Id", "admin")
                .header("X-Colaborador-Id", "2")
                .header("X-User-Extra", "qualquer-coisa")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.xUserId").isEqualTo("ana")
                .jsonPath("$.xUserRoles").isEqualTo("COLABORADOR")
                .jsonPath("$.xColaboradorId").isEqualTo("1")
                .jsonPath("$.xUserExtra").doesNotExist();
    }

    @Test
    @DisplayName("Admin chega no servico com X-Colaborador-Id vazio, mesmo se o cliente mandar um")
    void adminComColaboradorVazio() {
        client.get().uri("/api/cursos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + login("admin", "admin123"))
                .header("X-Colaborador-Id", "1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.xUserId").isEqualTo("admin")
                .jsonPath("$.xUserRoles").isEqualTo("ADMIN")
                .jsonPath("$.xColaboradorId").isEqualTo("");
    }

    @Test
    @DisplayName("Rota publica (Swagger) tambem remove headers de identidade forjados")
    void rotaPublicaRemoveHeadersForjados() {
        client.get().uri("/v3/api-docs")
                .header("X-User-Roles", "ADMIN")
                .header("X-User-Id", "admin")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.xUserId").doesNotExist()
                .jsonPath("$.xUserRoles").doesNotExist();
    }

    // ---------- roteamento ----------

    @Test
    @DisplayName("/api/ia/** vai para o ai-service, e o resto de /api/** vai para o Core")
    void roteamento() {
        String token = login("ana", "ana123");
        client.post().uri("/api/ia/recomendacoes").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.servico").isEqualTo("ai-service")
                .jsonPath("$.xUserId").isEqualTo("ana");
        client.get().uri("/api/alunos").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.servico").isEqualTo("core");
    }

    @Test
    @DisplayName("Fora de /api, /auth, docs e health, tudo e negado")
    void caminhoDesconhecidoNegado() {
        client.get().uri("/qualquer-coisa")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + login("admin", "admin123"))
                .exchange()
                .expectStatus().isForbidden();
    }

    // ---------- CORS e health ----------

    @Test
    @DisplayName("Preflight CORS do front (localhost:5173) e liberado")
    void preflightCors() {
        client.options().uri("/api/cursos")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173");
    }

    @Test
    @DisplayName("O 401 tambem leva header de CORS (o front consegue ler e fazer logout)")
    void respostaDe401TemCors() {
        client.get().uri("/api/cursos")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173");
    }

    @Test
    @DisplayName("Origem que nao e localhost e recusada no CORS")
    void origemDesconhecidaRecusada() {
        client.options().uri("/api/cursos")
                .header(HttpHeaders.ORIGIN, "http://site-malicioso.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Health check e publico (usado pelo healthcheck do compose)")
    void healthPublico() {
        client.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("UP");
    }

    private String login(String login, String senha) {
        LoginRespostaTeste resposta = client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("login", login, "senha", senha))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginRespostaTeste.class)
                .returnResult()
                .getResponseBody();
        assertThat(resposta).isNotNull();
        return resposta.token();
    }

    record LoginRespostaTeste(String token) {
    }
}
