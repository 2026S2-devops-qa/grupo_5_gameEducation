package com.example.gateway.auth;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Usuarios em memoria (plano, secao 5.3). Os ids de colaborador batem com o seed do Core:
 * ana = colaborador 1, carlos = colaborador 2.
 *
 * Limitacao conhecida: sem cadastro de usuarios e sem refresh token (secao 12 do plano).
 */
@Component
public class UsuariosDemo {

    /** Hash de uma senha qualquer, usado quando o login nao existe (ver autenticar). */
    private static final String HASH_FICTICIO = "$2a$10$9pmsKMLdpVdMr/DHa0hiiOXPTyYt/rnve1P3.jZCLK4OPBaxlau8u";

    private static final List<UsuarioDemo> USUARIOS = List.of(
            new UsuarioDemo("admin", "$2a$10$9pmsKMLdpVdMr/DHa0hiiOXPTyYt/rnve1P3.jZCLK4OPBaxlau8u",
                    List.of("ADMIN"), null),
            new UsuarioDemo("ana", "$2a$10$FOitX5pOKl3GrxFXYNxkAe4OEe8JetWeuOBLKgW94IMryqjN5zyjC",
                    List.of("COLABORADOR"), 1L),
            new UsuarioDemo("carlos", "$2a$10$tmiO3lujh67UeObtlqycjOo1pufar92qQDFHx1WZpQkEn8tJuk5Wm",
                    List.of("COLABORADOR"), 2L));

    private final Map<String, UsuarioDemo> porLogin = USUARIOS.stream()
            .collect(Collectors.toUnmodifiableMap(UsuarioDemo::login, Function.identity()));

    private final PasswordEncoder passwordEncoder;

    public UsuariosDemo(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Devolve o usuario se login e senha conferem. Quando o login nao existe, o BCrypt roda
     * do mesmo jeito contra um hash ficticio: assim o tempo de resposta nao revela quais
     * logins existem.
     */
    public Optional<UsuarioDemo> autenticar(String login, String senha) {
        UsuarioDemo usuario = login == null ? null : porLogin.get(login);
        String hash = usuario == null ? HASH_FICTICIO : usuario.senhaHash();
        boolean senhaConfere = senha != null && passwordEncoder.matches(senha, hash);
        return usuario != null && senhaConfere ? Optional.of(usuario) : Optional.empty();
    }
}
