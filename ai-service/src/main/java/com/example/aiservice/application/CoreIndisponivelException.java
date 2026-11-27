package com.example.aiservice.application;

/** O Core nao respondeu (fora do ar ou erro inesperado). Vira HTTP 503 fora das tools. */
public class CoreIndisponivelException extends RuntimeException {

    public CoreIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
