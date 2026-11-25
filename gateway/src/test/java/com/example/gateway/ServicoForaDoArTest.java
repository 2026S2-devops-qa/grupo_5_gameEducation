package com.example.gateway;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Com o servico de destino fora do ar, o gateway responde 503 {"erro": "..."} em vez de 500
 * (RNF04: a IA pode cair e o front mostra uma mensagem clara).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ServicoForaDoArTest {

    @DynamicPropertySource
    static void rotas(DynamicPropertyRegistry registry) {
        String semNinguemOuvindo = "http://localhost:" + portaLivre();
        registry.add("CORE_URL", () -> semNinguemOuvindo);
        registry.add("AI_URL", () -> semNinguemOuvindo);
    }

    @Autowired
    private WebTestClient client;

    @Test
    @DisplayName("ai-service fora do ar da 503 no formato {erro}")
    void servicoForaDoArDa503() {
        String token = client.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("login", "ana", "senha", "ana123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class).returnResult().getResponseBody()
                .get("token").toString();

        client.post().uri("/api/ia/recomendacoes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody().jsonPath("$.erro").value(erro ->
                        org.assertj.core.api.Assertions.assertThat(erro.toString()).contains("ai-service"));
    }

    private static int portaLivre() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
