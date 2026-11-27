package com.example.aiservice.application.port;

import com.example.aiservice.domain.CursoCatalogo;
import com.example.aiservice.domain.PerfilColaborador;
import com.example.aiservice.domain.Solicitante;
import java.util.List;
import java.util.Optional;

/**
 * Porta de saida: o que o agente le do Core (RF01). Toda chamada leva o {@link Solicitante}:
 * o adaptador REST repassa os headers do usuario logado, entao as regras de acesso do Core
 * (ex: colaborador so ve o proprio perfil) valem tambem para o agente (RNF03).
 *
 * Adaptadores: RestCoreGateway (Core real) e StubCoreGateway (dados fixos, enquanto o RF01
 * nao estiver na branch af).
 */
public interface CoreGateway {

    /** @throws com.example.aiservice.domain.AcessoNegadoException se o Core negar (403) */
    PerfilColaborador buscarPerfil(Solicitante solicitante, long colaboradorId);

    /** @param area DEVOPS, QA, DESENVOLVIMENTO, DADOS ou GESTAO; null para todas */
    List<CursoCatalogo> listarCursos(Solicitante solicitante, String area);

    Optional<CursoCatalogo> buscarCurso(Solicitante solicitante, long cursoId);
}
