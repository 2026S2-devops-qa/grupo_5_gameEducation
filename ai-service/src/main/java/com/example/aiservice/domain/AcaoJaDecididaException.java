package com.example.aiservice.domain;

/** A acao ja foi autorizada ou negada antes: nao pode ser decidida de novo. Vira HTTP 409. */
public class AcaoJaDecididaException extends RuntimeException {

    public AcaoJaDecididaException(String mensagem) {
        super(mensagem);
    }
}
