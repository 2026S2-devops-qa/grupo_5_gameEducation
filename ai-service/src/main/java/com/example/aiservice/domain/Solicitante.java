package com.example.aiservice.domain;

import java.util.List;
import java.util.Objects;

/**
 * Usuario logado, como o gateway repassou nos headers X-User-Id, X-User-Roles e X-Colaborador-Id
 * (secao 5.3 do plano). O ai-service nao valida JWT: confia no gateway, que e a unica entrada.
 *
 * @param login         login do usuario (X-User-Id)
 * @param papeis        papeis (X-User-Roles), ex: COLABORADOR ou ADMIN
 * @param colaboradorId id do colaborador ligado ao usuario (null para admin)
 */
public record Solicitante(String login, List<String> papeis, Long colaboradorId) {

    public Solicitante {
        Objects.requireNonNull(login, "login");
        papeis = papeis == null ? List.of() : List.copyOf(papeis);
    }

    public boolean admin() {
        return papeis.contains("ADMIN");
    }
}
