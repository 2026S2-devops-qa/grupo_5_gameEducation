package com.example.aiservice.domain;

/** Curso do catalogo do Core (GET /api/cursos, RF01). */
public record CursoCatalogo(Long id, String titulo, String area, String nivel, int cargaHoraria) {
}
