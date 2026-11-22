package com.example.gateway.security;

import java.util.List;
import java.util.Locale;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Repassa a identidade do usuario aos servicos internos (contrato da secao 5.3 do plano).
 *
 * Passo 1: REMOVE qualquer X-User-* ou X-Colaborador-Id que veio do cliente. Sem isso, qualquer
 * um mandaria "X-User-Roles: ADMIN" e os servicos (que confiam nos headers) achariam que e admin.
 * Passo 2: adiciona os valores verdadeiros, tirados do JWT que o Spring Security ja validou.
 */
@Component
public class IdentidadeHeadersFilter implements GlobalFilter, Ordered {

    public static final String X_USER_ID = "X-User-Id";
    public static final String X_USER_ROLES = "X-User-Roles";
    public static final String X_COLABORADOR_ID = "X-Colaborador-Id";

    private static final String PREFIXO_X_USER = "x-user-";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken())
                .map(jwt -> comIdentidade(exchange, jwt))
                .switchIfEmpty(Mono.fromSupplier(() -> semIdentidade(exchange)))
                .flatMap(chain::filter);
    }

    private ServerWebExchange comIdentidade(ServerWebExchange exchange, Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(SecurityConfig.CLAIM_ROLES);
        Object colaboradorId = jwt.getClaim(SecurityConfig.CLAIM_COLABORADOR_ID);
        return exchange.mutate().request(req -> req.headers(headers -> {
            removerHeadersDeIdentidade(headers);
            headers.set(X_USER_ID, jwt.getSubject());
            headers.set(X_USER_ROLES, roles == null ? "" : String.join(",", roles));
            // Vazio para quem nao tem colaborador ligado (admin), como diz o contrato.
            headers.set(X_COLABORADOR_ID, colaboradorId == null ? "" : colaboradorId.toString());
        })).build();
    }

    private ServerWebExchange semIdentidade(ServerWebExchange exchange) {
        return exchange.mutate().request(req -> req.headers(this::removerHeadersDeIdentidade)).build();
    }

    private void removerHeadersDeIdentidade(HttpHeaders headers) {
        List<String> forjados = headers.headerNames().stream()
                .filter(nome -> {
                    String minusculo = nome.toLowerCase(Locale.ROOT);
                    return minusculo.startsWith(PREFIXO_X_USER)
                            || minusculo.equals(X_COLABORADOR_ID.toLowerCase(Locale.ROOT));
                })
                .toList();
        forjados.forEach(headers::remove);
    }

    /** Roda cedo, antes dos filtros que encaminham a requisicao para o servico de destino. */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
