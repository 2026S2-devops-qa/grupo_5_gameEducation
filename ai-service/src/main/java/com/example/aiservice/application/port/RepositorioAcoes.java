package com.example.aiservice.application.port;

import com.example.aiservice.domain.AcaoAgente;
import java.util.List;
import java.util.Optional;

/** Porta de saida: onde as acoes do agente ficam guardadas (adaptador JPA + H2). */
public interface RepositorioAcoes {

    AcaoAgente salvar(AcaoAgente acao);

    Optional<AcaoAgente> buscar(long id);

    /** Acoes de um usuario, mais recentes primeiro. */
    List<AcaoAgente> listarDoUsuario(String login);

    /** Todas as acoes, mais recentes primeiro (visao do ADMIN). */
    List<AcaoAgente> listarTodas();
}
