package com.example.aiservice.adapters.in.web;

import com.example.aiservice.application.AcoesService;
import com.example.aiservice.application.OrientacaoService;
import com.example.aiservice.domain.Solicitante;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST do RF04 (contrato na secao 5.4 do plano). */
@RestController
@RequestMapping("/api/ia/agentes")
class AgenteController {

    private final OrientacaoService orientacaoService;
    private final AcoesService acoesService;

    AgenteController(OrientacaoService orientacaoService, AcoesService acoesService) {
        this.orientacaoService = orientacaoService;
        this.acoesService = acoesService;
    }

    @PostMapping("/orientacao")
    PlanoDeAcaoResponse orientar(
            @Valid @RequestBody OrientacaoRequest request,
            @RequestHeader(value = IdentidadeHeaders.USER_ID, required = false) String userId,
            @RequestHeader(value = IdentidadeHeaders.USER_ROLES, required = false) String roles,
            @RequestHeader(value = IdentidadeHeaders.COLABORADOR_ID, required = false) String colaboradorId) {
        Solicitante solicitante = IdentidadeHeaders.ler(userId, roles, colaboradorId);
        return PlanoDeAcaoResponse.from(orientacaoService.orientar(request.objetivo(), solicitante));
    }

    @PostMapping("/acoes/{id}/autorizar")
    AcaoAgenteResponse autorizar(
            @PathVariable long id,
            @RequestHeader(value = IdentidadeHeaders.USER_ID, required = false) String userId,
            @RequestHeader(value = IdentidadeHeaders.USER_ROLES, required = false) String roles,
            @RequestHeader(value = IdentidadeHeaders.COLABORADOR_ID, required = false) String colaboradorId) {
        return AcaoAgenteResponse.from(acoesService.autorizar(id, IdentidadeHeaders.ler(userId, roles, colaboradorId)));
    }

    @PostMapping("/acoes/{id}/negar")
    AcaoAgenteResponse negar(
            @PathVariable long id,
            @RequestHeader(value = IdentidadeHeaders.USER_ID, required = false) String userId,
            @RequestHeader(value = IdentidadeHeaders.USER_ROLES, required = false) String roles,
            @RequestHeader(value = IdentidadeHeaders.COLABORADOR_ID, required = false) String colaboradorId) {
        return AcaoAgenteResponse.from(acoesService.negar(id, IdentidadeHeaders.ler(userId, roles, colaboradorId)));
    }

    @GetMapping("/acoes")
    List<AcaoAgenteResponse> listar(
            @RequestHeader(value = IdentidadeHeaders.USER_ID, required = false) String userId,
            @RequestHeader(value = IdentidadeHeaders.USER_ROLES, required = false) String roles,
            @RequestHeader(value = IdentidadeHeaders.COLABORADOR_ID, required = false) String colaboradorId) {
        return acoesService.listar(IdentidadeHeaders.ler(userId, roles, colaboradorId)).stream()
                .map(AcaoAgenteResponse::from)
                .toList();
    }
}
