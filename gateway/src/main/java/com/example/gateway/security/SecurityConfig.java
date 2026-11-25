package com.example.gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Seguranca do gateway (RNF03): valida o JWT, aplica as regras por papel e cuida do CORS.
 *
 * O CORS fica aqui, na cadeia do Spring Security, e nao no globalcors do gateway: assim as
 * respostas 401 e 403 geradas pelo proprio Security tambem levam os headers de CORS. Sem isso
 * o navegador esconde o 401 do front, e o logout automatico nunca dispara.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    static final String CLAIM_ROLES = "roles";
    static final String CLAIM_COLABORADOR_ID = "colaboradorId";

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                // API stateless: cada requisicao traz o proprio token, nada fica em sessao.
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .cors(Customizer.withDefaults())
                .authorizeExchange(regras -> regras
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Rotas so de ADMIN (plano, secao 7.1, passo 4)
                        .pathMatchers(HttpMethod.POST, "/api/cursos").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/colaboradores").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/colaboradores").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/treinamentos").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/ranking/fechamentos/**").hasRole("ADMIN")
                        // Todo o resto da API exige so estar autenticado
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().denyAll())
                .oauth2ResourceServer(rs -> rs
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDePapeis()))
                        .authenticationEntryPoint(naoAutenticado()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(naoAutenticado())
                        .accessDeniedHandler(acessoNegado()))
                .build();
    }

    /** Le a claim "roles" do JWT e vira ROLE_ADMIN / ROLE_COLABORADOR para o hasRole(). */
    private ReactiveJwtAuthenticationConverterAdapter conversorDePapeis() {
        JwtGrantedAuthoritiesConverter papeis = new JwtGrantedAuthoritiesConverter();
        papeis.setAuthoritiesClaimName(CLAIM_ROLES);
        papeis.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(papeis);
        return new ReactiveJwtAuthenticationConverterAdapter(conversor);
    }

    private ServerAuthenticationEntryPoint naoAutenticado() {
        return (exchange, ex) -> {
            exchange.getResponse().getHeaders().set("WWW-Authenticate", "Bearer");
            return RespostaErro.escrever(exchange.getResponse(), HttpStatus.UNAUTHORIZED,
                    "Token ausente, invalido ou expirado. Faca login em /auth/login.");
        };
    }

    private ServerAccessDeniedHandler acessoNegado() {
        return (exchange, ex) -> RespostaErro.escrever(exchange.getResponse(), HttpStatus.FORBIDDEN,
                "Acesso negado: seu perfil nao tem permissao para esta operacao.");
    }

    @Bean
    SecretKey chaveJwt(JwtProperties props) {
        return new SecretKeySpec(props.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey chaveJwt) {
        return NimbusJwtEncoder.withSecretKey(chaveJwt).algorithm(MacAlgorithm.HS256).build();
    }

    /** Confere assinatura HS256, expiracao e emissor. Qualquer falha vira 401. */
    @Bean
    ReactiveJwtDecoder jwtDecoder(SecretKey chaveJwt, JwtProperties props) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(chaveJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.issuer()));
        return decoder;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(props.allowedOriginPatterns());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
