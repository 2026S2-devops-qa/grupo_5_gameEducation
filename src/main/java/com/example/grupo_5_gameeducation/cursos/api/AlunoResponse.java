package com.example.grupo_5_gameeducation.cursos.api;

import com.example.grupo_5_gameeducation.cursos.internal.AlunoEntity;
import com.example.grupo_5_gameeducation.domain.Plano;

public record AlunoResponse(
        Long id,
        String nome,
        Plano plano,
        int cursosLiberados,
        int cursosConquistados) {

    public static AlunoResponse from(AlunoEntity entity) {
        return new AlunoResponse(
                entity.getId(),
                entity.getNome(),
                entity.getPlano(),
                entity.getCursosLiberados(),
                entity.getCursosConquistados());
    }
}
