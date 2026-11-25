package com.example.gateway.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao do JWT (prefixo gateway.jwt). O segredo vem da variavel JWT_SECRET.
 */
@ConfigurationProperties("gateway.jwt")
public record JwtProperties(String secret, Duration expiracao, String issuer) {

    /** HS256 usa chave de 256 bits: menos que isso e inseguro e o Nimbus recusa. */
    static final int TAMANHO_MINIMO_SEGREDO_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET precisa ter pelo menos " + TAMANHO_MINIMO_SEGREDO_BYTES + " bytes (HS256)");
        }
    }
}
