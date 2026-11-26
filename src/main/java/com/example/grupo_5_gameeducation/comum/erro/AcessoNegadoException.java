package com.example.grupo_5_gameeducation.comum.erro;

/** Usuario autenticado, mas sem permissao para este dado: vira 403 {"erro": "..."}. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
