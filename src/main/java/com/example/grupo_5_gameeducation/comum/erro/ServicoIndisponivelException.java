package com.example.grupo_5_gameeducation.comum.erro;

/** Dependencia externa fora do ar (ex.: ml-service): vira 503 {"erro": "..."}. */
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public ServicoIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
