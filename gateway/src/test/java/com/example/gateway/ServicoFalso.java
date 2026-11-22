package com.example.gateway;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * Servidor HTTP de teste que faz o papel de um servico interno (Core ou ai-service).
 * Devolve em JSON o que recebeu: nome do servico, caminho e os headers de identidade.
 * Assim o teste ve exatamente o que o gateway repassou.
 */
final class ServicoFalso {

    private final DisposableServer servidor;

    private ServicoFalso(DisposableServer servidor) {
        this.servidor = servidor;
    }

    static ServicoFalso iniciar(String nome) {
        DisposableServer servidor = HttpServer.create()
                .port(0)
                .handle((req, res) -> {
                    String json = "{" + Stream.of(
                            campo("servico", nome),
                            campo("path", req.uri()),
                            campo("xUserId", req.requestHeaders().get("X-User-Id")),
                            campo("xUserRoles", req.requestHeaders().get("X-User-Roles")),
                            campo("xColaboradorId", req.requestHeaders().get("X-Colaborador-Id")),
                            campo("xUserExtra", req.requestHeaders().get("X-User-Extra")))
                            .collect(Collectors.joining(",")) + "}";
                    return res.header("Content-Type", "application/json").sendString(Mono.just(json));
                })
                .bindNow();
        return new ServicoFalso(servidor);
    }

    String url() {
        return "http://localhost:" + servidor.port();
    }

    void parar() {
        servidor.disposeNow();
    }

    private static String campo(String nome, String valor) {
        return "\"" + nome + "\":" + (valor == null ? "null" : "\"" + valor.replace("\"", "\\\"") + "\"");
    }
}
