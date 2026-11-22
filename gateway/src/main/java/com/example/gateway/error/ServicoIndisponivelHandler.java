package com.example.gateway.error;

import com.example.gateway.security.RespostaErro;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * Quando o servico de destino esta fora do ar, responde 503 {"erro": "..."} em vez de um 500
 * generico. E o que permite ao front mostrar "IA indisponivel" enquanto cadastro e consulta
 * continuam funcionando (RNF04). Timeout vira 504. Qualquer outro erro segue para o handler
 * padrao do Spring Boot.
 *
 * Roda antes do handler padrao (que tem ordem -1), por isso a ordem -2.
 */
@Component
@Order(-2)
public class ServicoIndisponivelHandler implements WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ServicoIndisponivelHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        Route rota = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (rota == null) {
            return Mono.error(ex);
        }
        String servico = rota.getId();
        if (temCausa(ex, ConnectException.class) || temCausa(ex, UnknownHostException.class)
                || temCausa(ex, UnresolvedAddressException.class)) {
            log.warn("Servico '{}' indisponivel ({}): {}", servico, rota.getUri(), ex.toString());
            return RespostaErro.escrever(exchange.getResponse(), HttpStatus.SERVICE_UNAVAILABLE,
                    "Servico '" + servico + "' indisponivel no momento. Tente novamente em instantes.");
        }
        if (temCausa(ex, TimeoutException.class)) {
            log.warn("Servico '{}' nao respondeu a tempo: {}", servico, ex.toString());
            return RespostaErro.escrever(exchange.getResponse(), HttpStatus.GATEWAY_TIMEOUT,
                    "Servico '" + servico + "' nao respondeu a tempo.");
        }
        return Mono.error(ex);
    }

    private static boolean temCausa(Throwable ex, Class<? extends Throwable> tipo) {
        for (Throwable atual = ex; atual != null; atual = atual.getCause()) {
            if (tipo.isInstance(atual)) {
                return true;
            }
            if (atual.getCause() == atual) {
                break;
            }
        }
        return false;
    }
}
