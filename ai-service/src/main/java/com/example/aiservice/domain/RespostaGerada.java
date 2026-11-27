package com.example.aiservice.domain;

import java.util.List;

/**
 * O que o modelo de linguagem devolve, ainda sem nenhuma checagem (o "G" do RAG).
 * A {@link Recomendacao} final so aproveita o que estiver fundamentado nos trechos.
 */
public record RespostaGerada(String resposta, List<CursoRecomendado> cursos) {

    public RespostaGerada {
        cursos = cursos == null ? List.of() : List.copyOf(cursos);
    }
}
