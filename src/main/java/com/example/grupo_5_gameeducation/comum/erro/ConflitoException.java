package com.example.grupo_5_gameeducation.comum.erro;

/** Conflita com o estado atual (ex.: titulo ou email ja cadastrado, mes ja fechado): vira 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
