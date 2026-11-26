package com.example.grupo_5_gameeducation.cursos.api;

import com.example.grupo_5_gameeducation.domain.Plano;

/**
 * Contrato publico de saida. Nao conhece a entidade JPA: a conversao AlunoEntity -> AlunoResponse
 * fica no cursos.internal (AlunoMapper). A dependencia so vai de internal para api, nunca o contrario.
 */
public record AlunoResponse(
        Long id,
        String nome,
        Plano plano,
        int cursosLiberados,
        int cursosConquistados) {
}
