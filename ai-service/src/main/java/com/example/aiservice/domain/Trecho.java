package com.example.aiservice.domain;

import java.util.Objects;

/**
 * Pedaco de um documento institucional recuperado pela busca semantica (o "R" do RAG).
 *
 * @param fonte nome do arquivo de origem (ex: "politica-de-trilhas.md"), usado para citar a fonte
 * @param texto conteudo do pedaco
 * @param score similaridade com a pergunta (0 a 1), quando a base informar
 */
public record Trecho(String fonte, String texto, double score) {

    public Trecho {
        Objects.requireNonNull(fonte, "fonte");
        Objects.requireNonNull(texto, "texto");
    }

    /** O trecho cita o titulo de curso? Comparacao sem diferenciar maiusculas e minusculas. */
    public boolean menciona(String tituloCurso) {
        return tituloCurso != null
                && !tituloCurso.isBlank()
                && texto.toLowerCase().contains(tituloCurso.strip().toLowerCase());
    }
}
