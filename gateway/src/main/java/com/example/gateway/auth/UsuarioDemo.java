package com.example.gateway.auth;

import java.util.List;

/**
 * Usuario fixo de demonstracao. senhaHash e BCrypt: a senha em texto nunca fica no codigo.
 * colaboradorId liga o login ao colaborador do Core (null para o admin).
 */
public record UsuarioDemo(String login, String senhaHash, List<String> roles, Long colaboradorId) {
}
