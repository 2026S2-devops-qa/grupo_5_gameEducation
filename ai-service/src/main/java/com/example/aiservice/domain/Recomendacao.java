package com.example.aiservice.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Recomendacao de cursos devolvida ao colaborador (RF03).
 *
 * Regra de negocio contra alucinacao: so entram cursos que aparecem nos trechos recuperados,
 * e as fontes sao os arquivos de onde os trechos vieram (nunca o texto livre do LLM).
 */
public record Recomendacao(String resposta, List<CursoRecomendado> cursos, List<String> fontes) {

    public static final String SEM_BASE =
            "Não encontrei nos documentos institucionais informações para recomendar cursos para esse objetivo.";

    public Recomendacao {
        cursos = List.copyOf(cursos);
        fontes = List.copyOf(fontes);
    }

    /** Nenhum trecho relevante: responde sem inventar curso e sem citar fonte. */
    public static Recomendacao semBase() {
        return new Recomendacao(SEM_BASE, List.of(), List.of());
    }

    /**
     * Monta a recomendacao a partir do que o LLM gerou, mantendo so o que esta nos trechos.
     * Cursos repetidos (mesmo titulo) aparecem uma vez so.
     */
    public static Recomendacao fundamentada(RespostaGerada gerada, List<Trecho> trechos) {
        if (trechos.isEmpty()) {
            return semBase();
        }

        List<CursoRecomendado> cursos = new ArrayList<>();
        Set<String> titulosVistos = new LinkedHashSet<>();
        for (CursoRecomendado curso : gerada.cursos()) {
            if (curso == null || curso.titulo() == null) {
                continue;
            }
            boolean citadoNosTrechos = trechos.stream().anyMatch(t -> t.menciona(curso.titulo()));
            if (citadoNosTrechos && titulosVistos.add(curso.titulo().strip().toLowerCase())) {
                cursos.add(new CursoRecomendado(curso.titulo().strip(), curso.motivo()));
            }
        }

        List<String> fontes = trechos.stream().map(Trecho::fonte).distinct().toList();

        String resposta = gerada.resposta() == null || gerada.resposta().isBlank() ? SEM_BASE : gerada.resposta();
        return new Recomendacao(resposta, cursos, fontes);
    }
}
