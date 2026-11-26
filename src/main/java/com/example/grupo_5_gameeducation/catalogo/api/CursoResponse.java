package com.example.grupo_5_gameeducation.catalogo.api;

/**
 * Contrato da secao 5.4:
 * {"id": 1, "titulo": "Docker na Pratica", "area": "DEVOPS", "nivel": "INTERMEDIARIO", "cargaHoraria": 20}
 */
public record CursoResponse(Long id, String titulo, Area area, Nivel nivel, int cargaHoraria) {

    public static CursoResponse from(CursoResumo curso) {
        return new CursoResponse(curso.id(), curso.titulo(), curso.area(), curso.nivel(), curso.cargaHoraria());
    }
}
