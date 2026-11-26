package com.example.grupo_5_gameeducation.comum.erro;

/** Recurso nao existe: vira 404 {"erro": "..."}. */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
