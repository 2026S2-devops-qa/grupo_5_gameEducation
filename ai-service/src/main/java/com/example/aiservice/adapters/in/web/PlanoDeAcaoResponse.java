package com.example.aiservice.adapters.in.web;

import com.example.aiservice.domain.PlanoDeAcao;
import java.util.List;

/** JSON de POST /api/ia/agentes/orientacao: resumo, acoes propostas (aguardando) e tools usadas. */
public record PlanoDeAcaoResponse(String resumo, List<AcaoAgenteResponse> acoes, List<String> toolsUsadas) {

    static PlanoDeAcaoResponse from(PlanoDeAcao plano) {
        return new PlanoDeAcaoResponse(plano.resumo(),
                plano.acoes().stream().map(AcaoAgenteResponse::from).toList(),
                plano.toolsUsadas());
    }
}
