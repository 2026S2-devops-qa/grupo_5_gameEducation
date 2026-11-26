package com.example.grupo_5_gameeducation.cursos.internal;

import com.example.grupo_5_gameeducation.cursos.api.AlunoResponse;

/**
 * Converte a entidade interna no contrato publico. Antes isso era AlunoResponse.from(AlunoEntity),
 * o que fazia o api depender do internal (ponto do feedback da AC2). Agora o sentido e o certo:
 * internal -> api.
 */
final class AlunoMapper {

    private AlunoMapper() {
    }

    static AlunoResponse paraResponse(AlunoEntity entity) {
        return new AlunoResponse(
                entity.getId(),
                entity.getNome(),
                entity.getPlano(),
                entity.getCursosLiberados(),
                entity.getCursosConquistados());
    }
}
