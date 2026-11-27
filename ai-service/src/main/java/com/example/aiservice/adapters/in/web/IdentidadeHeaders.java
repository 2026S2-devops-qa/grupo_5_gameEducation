package com.example.aiservice.adapters.in.web;

import com.example.aiservice.domain.Solicitante;
import java.util.Arrays;
import java.util.List;

/**
 * Le a identidade que o gateway repassa (secao 5.3): X-User-Id, X-User-Roles, X-Colaborador-Id.
 * O gateway sobrescreve esses headers, e o ai-service nao publica porta, entao so chega aqui
 * quem passou pelo gateway com um JWT valido.
 */
final class IdentidadeHeaders {

    static final String USER_ID = "X-User-Id";
    static final String USER_ROLES = "X-User-Roles";
    static final String COLABORADOR_ID = "X-Colaborador-Id";

    private IdentidadeHeaders() {
    }

    static Solicitante ler(String userId, String roles, String colaboradorId) {
        if (userId == null || userId.isBlank()) {
            throw new IdentidadeAusenteException();
        }
        List<String> papeis = roles == null ? List.of() : Arrays.stream(roles.split(","))
                .map(String::strip)
                .filter(p -> !p.isEmpty())
                .toList();
        return new Solicitante(userId.strip(), papeis, colaborador(colaboradorId));
    }

    private static Long colaborador(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(valor.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Header " + COLABORADOR_ID + " inválido.");
        }
    }

    /** Requisicao sem identidade: nao veio pelo gateway. Vira HTTP 401. */
    static class IdentidadeAusenteException extends RuntimeException {
        IdentidadeAusenteException() {
            super("Identidade ausente: chame pelo gateway (header " + USER_ID + ").");
        }
    }
}
