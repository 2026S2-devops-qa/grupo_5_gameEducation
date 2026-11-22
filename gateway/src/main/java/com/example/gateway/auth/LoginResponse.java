package com.example.gateway.auth;

import java.util.List;

/** Contrato da secao 5.4: {"token":"...","login":"ana","roles":["COLABORADOR"],"colaboradorId":1}. */
public record LoginResponse(String token, String login, List<String> roles, Long colaboradorId) {
}
