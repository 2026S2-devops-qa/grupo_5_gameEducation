package com.example.grupo_5_gameeducation.catalogo.api;

/** Curso como os outros modulos enxergam: dado publico do contrato, sem nada de JPA. */
public record CursoResumo(Long id, String titulo, Area area, Nivel nivel, int cargaHoraria) {
}
