package com.example.aiservice.domain;

/**
 * Perfil do colaborador como o Core devolve (GET /api/colaboradores/{id}, RF01).
 * O email nao entra aqui de proposito: o agente nao precisa dele (minimizacao de dados, RNF03).
 */
public record PerfilColaborador(
        Long id,
        String nome,
        String cargo,
        String area,
        int anosExperiencia,
        int cursosConcluidos,
        double mediaNotas) {
}
