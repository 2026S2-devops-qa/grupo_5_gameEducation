package com.example.grupo_5_gameeducation.colaboradores.api;

import com.example.grupo_5_gameeducation.catalogo.api.Area;

/**
 * Perfil profissional do colaborador, usado por outros modulos (ex.: competencias, RF02,
 * manda anosExperiencia, cursosConcluidos e mediaNotas para o modelo de ML).
 *
 * Nao tem email nem outro dado de contato: quem so precisa do perfil nao recebe dado pessoal
 * que nao vai usar (minimizacao, LGPD, RNF03).
 */
public record PerfilColaborador(
        Long id,
        String nome,
        Area area,
        int anosExperiencia,
        int cursosConcluidos,
        double mediaNotas) {
}
