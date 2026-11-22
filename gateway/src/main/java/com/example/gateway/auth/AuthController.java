package com.example.gateway.auth;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /auth/login: atendido pelo proprio gateway (nao e roteado para nenhum servico).
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuariosDemo usuarios;
    private final TokenService tokenService;

    public AuthController(UsuariosDemo usuarios, TokenService tokenService) {
        this.usuarios = usuarios;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody(required = false) LoginRequest request) {
        if (request == null || vazio(request.login()) || vazio(request.senha())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe login e senha."));
        }
        return usuarios.autenticar(request.login(), request.senha())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(
                        new LoginResponse(tokenService.emitir(u), u.login(), u.roles(), u.colaboradorId())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Login ou senha invalidos.")));
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
