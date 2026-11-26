package com.example.grupo_5_gameeducation.colaboradores.api;

import com.example.grupo_5_gameeducation.catalogo.api.Area;

/**
 * Contrato da secao 5.4. O email vem mascarado ("a***@empresa.com") para quem nao e ADMIN.
 */
public record ColaboradorResponse(
        Long id,
        String nome,
        String email,
        String cargo,
        Area area,
        int anosExperiencia,
        int cursosConcluidos,
        double mediaNotas) {
}
