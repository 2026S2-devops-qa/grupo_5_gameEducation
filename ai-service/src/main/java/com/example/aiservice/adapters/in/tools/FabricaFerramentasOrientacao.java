package com.example.aiservice.adapters.in.tools;

import com.example.aiservice.application.AcoesService;
import com.example.aiservice.application.port.BaseConhecimento;
import com.example.aiservice.application.port.CoreGateway;
import com.example.aiservice.application.port.FabricaDeFerramentas;
import com.example.aiservice.domain.Solicitante;
import org.springframework.stereotype.Component;

/** Cria as tools do agente para cada requisicao, presas ao usuario logado. */
@Component
class FabricaFerramentasOrientacao implements FabricaDeFerramentas {

    private final CoreGateway coreGateway;
    private final BaseConhecimento baseConhecimento;
    private final AcoesService acoesService;

    FabricaFerramentasOrientacao(CoreGateway coreGateway, BaseConhecimento baseConhecimento,
                                 AcoesService acoesService) {
        this.coreGateway = coreGateway;
        this.baseConhecimento = baseConhecimento;
        this.acoesService = acoesService;
    }

    @Override
    public Sessao criar(Solicitante solicitante) {
        return new FerramentasOrientacao(solicitante, coreGateway, baseConhecimento, acoesService);
    }
}
