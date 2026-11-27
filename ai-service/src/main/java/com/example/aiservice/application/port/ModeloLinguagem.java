package com.example.aiservice.application.port;

import com.example.aiservice.domain.RespostaGerada;

/**
 * Porta de saida: o modelo de linguagem (o "G" do RAG).
 * Implementada por SpringAiModeloLinguagem (ChatClient). Trocar OpenAI por Gemini ou Ollama
 * e so configuracao do adaptador (.env), o nucleo nao muda.
 */
public interface ModeloLinguagem {

    /** Existe chave de LLM configurada? Sem ela, as rotas de IA respondem 503. */
    boolean configurado();

    /**
     * Gera a resposta estruturada a partir das instrucoes de sistema e da mensagem do usuario.
     *
     * @throws com.example.aiservice.application.IaIndisponivelException se o LLM falhar
     */
    RespostaGerada gerar(String instrucoesSistema, String mensagemUsuario);
}
