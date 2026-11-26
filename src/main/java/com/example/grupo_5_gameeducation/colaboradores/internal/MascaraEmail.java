package com.example.grupo_5_gameeducation.colaboradores.internal;

/**
 * Mascara o email para quem nao e ADMIN (RNF03): "ana@empresa.com" vira "a***@empresa.com".
 * Mantem so a primeira letra e o dominio, o suficiente para reconhecer, sem expor o endereco.
 */
final class MascaraEmail {

    private static final String OCULTO = "***";

    private MascaraEmail() {
    }

    static String mascarar(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        int arroba = email.indexOf('@');
        if (arroba <= 0) {
            return OCULTO;
        }
        return email.charAt(0) + OCULTO + email.substring(arroba);
    }
}
