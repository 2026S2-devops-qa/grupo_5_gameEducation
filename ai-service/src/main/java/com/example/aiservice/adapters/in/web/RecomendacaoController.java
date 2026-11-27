package com.example.aiservice.adapters.in.web;

import com.example.aiservice.application.RecomendacaoService;
import com.example.aiservice.domain.Solicitante;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST do RF03 (contrato na secao 5.4 do plano). */
@RestController
@RequestMapping("/api/ia")
class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    RecomendacaoController(RecomendacaoService recomendacaoService) {
        this.recomendacaoService = recomendacaoService;
    }

    @PostMapping("/recomendacoes")
    RecomendacaoResponse recomendar(
            @Valid @RequestBody RecomendacaoRequest request,
            @RequestHeader(value = IdentidadeHeaders.USER_ID, required = false) String userId,
            @RequestHeader(value = IdentidadeHeaders.USER_ROLES, required = false) String roles,
            @RequestHeader(value = IdentidadeHeaders.COLABORADOR_ID, required = false) String colaboradorId) {
        Solicitante solicitante = IdentidadeHeaders.ler(userId, roles, colaboradorId);
        return RecomendacaoResponse.from(recomendacaoService.recomendar(request.objetivo(), solicitante));
    }
}
