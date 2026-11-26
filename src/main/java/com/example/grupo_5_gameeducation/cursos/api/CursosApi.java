package com.example.grupo_5_gameeducation.cursos.api;

import java.util.List;

/**
 * Contrato publico do modulo cursos (porta de entrada da arquitetura hexagonal).
 *
 * O {@link AlunoController} depende so desta interface, e nao do cursos.internal: e a correcao
 * do ponto do feedback da AC2 ("dependencia entre o contrato publico e a implementacao interna").
 * Outros modulos tambem usam so esta interface (ex.: o ranking vai chamar um bonificar aqui).
 */
public interface CursosApi {

    AlunoResponse criar(CriarAlunoRequest request);

    List<AlunoResponse> listar();

    /** @throws com.example.grupo_5_gameeducation.comum.erro.NaoEncontradoException se nao existir (404) */
    AlunoResponse buscarPorId(Long id);

    /**
     * Registra a conclusao de um curso e aplica a regra da US1. O curso precisa existir no
     * catalogo (validado pelo contrato publico do modulo catalogo).
     *
     * @throws com.example.grupo_5_gameeducation.comum.erro.RequisicaoInvalidaException curso fora do catalogo (400)
     */
    AlunoResponse concluirCurso(Long id, ConcluirCursoRequest request);
}
