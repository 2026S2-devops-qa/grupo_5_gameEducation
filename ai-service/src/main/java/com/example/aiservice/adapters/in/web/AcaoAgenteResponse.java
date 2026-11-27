package com.example.aiservice.adapters.in.web;

import com.example.aiservice.domain.AcaoAgente;
import com.example.aiservice.domain.StatusAcao;
import com.example.aiservice.domain.TipoAcao;
import java.time.Instant;

/** JSON de uma acao do agente (RF04). */
public record AcaoAgenteResponse(
        Long id,
        TipoAcao tipo,
        StatusAcao status,
        Long colaboradorId,
        String solicitadoPor,
        String descricao,
        String motivo,
        Long cursoId,
        Integer dias,
        Instant criadoEm,
        String autorizadoPor,
        Instant autorizadoEm,
        String resultado) {

    static AcaoAgenteResponse from(AcaoAgente a) {
        return new AcaoAgenteResponse(a.getId(), a.getTipo(), a.getStatus(), a.getColaboradorId(),
                a.getSolicitadoPor(), a.getDescricao(), a.getMotivo(), a.getCursoId(), a.getDias(),
                a.getCriadoEm(), a.getAutorizadoPor(), a.getAutorizadoEm(), a.getResultado());
    }
}
