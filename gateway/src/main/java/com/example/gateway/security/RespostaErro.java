package com.example.gateway.security;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

/**
 * Escreve erros no formato unico do projeto: {"erro": "mensagem legivel"} (plano, secao 5.4).
 */
public final class RespostaErro {

    private RespostaErro() {
    }

    public static Mono<Void> escrever(ServerHttpResponse response, HttpStatus status, String mensagem) {
        if (response.isCommitted()) {
            return Mono.empty();
        }
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] corpo = json(mensagem).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(corpo);
        return response.writeWith(Mono.just(buffer));
    }

    static String json(String mensagem) {
        String escapada = mensagem.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"erro\":\"" + escapada + "\"}";
    }
}
