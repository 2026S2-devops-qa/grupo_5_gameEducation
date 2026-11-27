package com.example.aiservice.application;

/** A IA nao pode atender agora (sem chave, base nao pronta ou LLM fora). Vira HTTP 503. */
public class IaIndisponivelException extends RuntimeException {

    public static final String SEM_CHAVE = "LLM não configurado: defina OPENAI_API_KEY no .env";
    public static final String BASE_NAO_PRONTA =
            "A base de conhecimento ainda não está pronta. Veja o log do ai-service (ingestão dos documentos).";
    public static final String FALHA_LLM = "Não consegui consultar o LLM agora. Tente de novo em instantes.";

    public IaIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public IaIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
