package com.example.grupo_5_gameeducation.comum.identidade;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Quem esta fazendo a requisicao, montado a partir dos headers que o API Gateway grava
 * depois de validar o JWT (plano, secao 5.3). O Core nao valida JWT: confia nesses headers,
 * porque o gateway apaga qualquer valor forjado pelo cliente e o Core nao publica porta.
 *
 * @param login         X-User-Id
 * @param roles         X-User-Roles (separados por virgula)
 * @param colaboradorId X-Colaborador-Id (null para quem nao e colaborador, ex.: admin)
 */
public record Solicitante(String login, Set<String> roles, Long colaboradorId) {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_ROLES = "X-User-Roles";
    public static final String HEADER_COLABORADOR_ID = "X-Colaborador-Id";

    public static final String ADMIN = "ADMIN";

    public Solicitante {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    /** Monta a partir dos valores crus dos headers (qualquer um pode vir null ou vazio). */
    public static Solicitante dosHeaders(String userId, String roles, String colaboradorId) {
        Set<String> papeis = roles == null ? Set.of() : Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(papel -> !papel.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        return new Solicitante(vazioParaNull(userId), papeis, paraLong(colaboradorId));
    }

    public boolean isAdmin() {
        return roles.contains(ADMIN);
    }

    /** true se for o proprio colaborador dono do dado. */
    public boolean ehOColaborador(Long id) {
        return colaboradorId != null && colaboradorId.equals(id);
    }

    /**
     * Regra de acesso a dados de um colaborador (RNF03): ADMIN ou o proprio colaborador.
     * Use em qualquer rota /api/colaboradores/{id}/... (ex.: competencia, no RF02).
     */
    public boolean podeAcessarColaborador(Long id) {
        return isAdmin() || ehOColaborador(id);
    }

    private static String vazioParaNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static Long paraLong(String valor) {
        String limpo = vazioParaNull(valor);
        if (limpo == null) {
            return null;
        }
        try {
            return Long.valueOf(limpo);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
