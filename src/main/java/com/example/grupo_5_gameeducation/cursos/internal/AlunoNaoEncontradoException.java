package com.example.grupo_5_gameeducation.cursos.internal;

import com.example.grupo_5_gameeducation.comum.erro.NaoEncontradoException;

/** Vira 404 {"erro": "Aluno nao encontrado: id"} pelo handler comum. */
public class AlunoNaoEncontradoException extends NaoEncontradoException {

    public AlunoNaoEncontradoException(Long id) {
        super("Aluno nao encontrado: " + id);
    }
}
