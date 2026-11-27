package com.example.aiservice.domain;

/**
 * Curso sugerido na recomendacao.
 *
 * @param titulo titulo do curso, igual ao do catalogo do Core (seed do RF01)
 * @param motivo por que o curso ajuda no objetivo do colaborador
 */
public record CursoRecomendado(String titulo, String motivo) {
}
