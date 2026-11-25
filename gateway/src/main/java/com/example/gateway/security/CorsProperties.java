package com.example.gateway.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Origens liberadas para o navegador (prefixo gateway.cors). O CORS existe so no gateway:
 * o navegador nunca fala direto com Core, ai-service ou ml-service.
 */
@ConfigurationProperties("gateway.cors")
public record CorsProperties(List<String> allowedOriginPatterns) {
}
