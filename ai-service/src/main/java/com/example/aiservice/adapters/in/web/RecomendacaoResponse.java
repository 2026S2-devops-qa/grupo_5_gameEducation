package com.example.aiservice.adapters.in.web;

import com.example.aiservice.domain.Recomendacao;
import java.util.List;

/**
 * Resposta de POST /api/ia/recomendacoes (secao 5.4):
 * {"resposta":"...","cursos":[{"titulo":"Docker na Prática","motivo":"..."}],"fontes":["politica-de-trilhas.md"]}
 */
public record RecomendacaoResponse(String resposta, List<CursoItem> cursos, List<String> fontes) {

    public record CursoItem(String titulo, String motivo) {
    }

    static RecomendacaoResponse from(Recomendacao recomendacao) {
        List<CursoItem> cursos = recomendacao.cursos().stream()
                .map(c -> new CursoItem(c.titulo(), c.motivo()))
                .toList();
        return new RecomendacaoResponse(recomendacao.resposta(), cursos, recomendacao.fontes());
    }
}
