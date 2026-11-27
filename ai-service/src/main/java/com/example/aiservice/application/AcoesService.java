package com.example.aiservice.application;

import com.example.aiservice.application.port.CoreGateway;
import com.example.aiservice.application.port.RepositorioAcoes;
import com.example.aiservice.domain.AcaoAgente;
import com.example.aiservice.domain.CursoCatalogo;
import com.example.aiservice.domain.Solicitante;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Casos de uso das acoes do agente (RF04): propor (chamado pelas tools), autorizar, negar e listar.
 * Propor so cria a acao em AGUARDANDO_AUTORIZACAO; quem executa e o usuario, em autorizar.
 */
@Service
public class AcoesService {

    private static final Logger log = LoggerFactory.getLogger(AcoesService.class);

    private final RepositorioAcoes repositorio;
    private final CoreGateway coreGateway;
    private final Clock relogio;

    public AcoesService(RepositorioAcoes repositorio, CoreGateway coreGateway, Clock relogio) {
        this.repositorio = repositorio;
        this.coreGateway = coreGateway;
        this.relogio = relogio;
    }

    /** A inscricao precisa ser num curso que existe no catalogo do Core. */
    public AcaoAgente proporInscricao(Solicitante solicitante, long cursoId, String motivo) {
        CursoCatalogo curso = coreGateway.buscarCurso(solicitante, cursoId)
                .orElseThrow(() -> new NaoEncontradaException("O curso " + cursoId + " não existe no catálogo."));
        return registrar(AcaoAgente.proporInscricao(solicitante, curso, motivo, relogio.instant()));
    }

    public AcaoAgente proporAcompanhamento(Solicitante solicitante, int dias, String motivo) {
        return registrar(AcaoAgente.proporAcompanhamento(solicitante, dias, motivo, relogio.instant()));
    }

    public AcaoAgente autorizar(long id, Solicitante solicitante) {
        AcaoAgente acao = buscar(id);
        acao.autorizar(solicitante, relogio.instant());
        log.info("AGENTE acao={} AUTORIZADA por {}: {}", id, solicitante.login(), acao.getResultado());
        return repositorio.salvar(acao);
    }

    public AcaoAgente negar(long id, Solicitante solicitante) {
        AcaoAgente acao = buscar(id);
        acao.negar(solicitante, relogio.instant());
        log.info("AGENTE acao={} NEGADA por {}", id, solicitante.login());
        return repositorio.salvar(acao);
    }

    /** O usuario ve as proprias acoes; o ADMIN ve todas. */
    public List<AcaoAgente> listar(Solicitante solicitante) {
        return solicitante.admin() ? repositorio.listarTodas() : repositorio.listarDoUsuario(solicitante.login());
    }

    private AcaoAgente registrar(AcaoAgente proposta) {
        AcaoAgente salva = repositorio.salvar(proposta);
        log.info("AGENTE acao={} PROPOSTA para {} ({}): {}", salva.getId(), salva.getSolicitadoPor(),
                salva.getStatus(), salva.getDescricao());
        return salva;
    }

    private AcaoAgente buscar(long id) {
        return repositorio.buscar(id)
                .orElseThrow(() -> new NaoEncontradaException("Ação " + id + " não encontrada."));
    }
}
