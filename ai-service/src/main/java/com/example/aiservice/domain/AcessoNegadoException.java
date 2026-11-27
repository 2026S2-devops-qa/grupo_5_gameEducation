package com.example.aiservice.domain;

/** O usuario tentou ver ou decidir algo que nao e dele. Vira HTTP 403. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
