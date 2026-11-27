package com.example.aiservice.domain;

import java.util.List;

/**
 * Resposta do agente de orientacao (RF04): a explicacao, as acoes que ele PROPOS
 * (todas em AGUARDANDO_AUTORIZACAO) e quais tools ele usou para chegar nelas.
 */
public record PlanoDeAcao(String resumo, List<AcaoAgente> acoes, List<String> toolsUsadas) {

    public PlanoDeAcao {
        acoes = List.copyOf(acoes);
        toolsUsadas = List.copyOf(toolsUsadas);
    }
}
