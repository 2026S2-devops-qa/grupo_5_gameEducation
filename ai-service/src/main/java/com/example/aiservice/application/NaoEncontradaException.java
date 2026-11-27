package com.example.aiservice.application;

/** Recurso inexistente (acao, curso, colaborador). Vira HTTP 404. */
public class NaoEncontradaException extends RuntimeException {

    public NaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
