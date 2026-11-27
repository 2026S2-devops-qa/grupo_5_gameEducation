package com.example.aiservice.application.port;

import com.example.aiservice.domain.AcaoAgente;
import com.example.aiservice.domain.Solicitante;
import java.util.List;

/**
 * Cria, para UMA requisicao, o conjunto de tools que o agente pode usar, ja preso ao usuario logado.
 * Por isso as tools nao recebem colaboradorId como parametro: o LLM nao tem como escolher outra
 * pessoa, mesmo que o texto do usuario peca (protecao contra prompt injection, RNF03).
 */
public interface FabricaDeFerramentas {

    Sessao criar(Solicitante solicitante);

    interface Sessao {

        /** Objeto com os metodos @Tool, entregue ao ModeloLinguagem (o nucleo nao conhece o tipo). */
        Object ferramentas();

        /** Nomes das tools chamadas nesta requisicao, na ordem. */
        List<String> toolsUsadas();

        /** Acoes que as tools de acao propuseram nesta requisicao (todas aguardando autorizacao). */
        List<AcaoAgente> acoesPropostas();
    }
}
