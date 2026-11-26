package com.example.grupo_5_gameeducation.comum.erro;

/** Pedido que fere uma regra de negocio ou tem dado invalido: vira 400 {"erro": "..."}. */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
