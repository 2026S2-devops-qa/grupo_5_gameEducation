package com.example.aiservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiservice.application.port.CoreGateway;
import com.example.aiservice.application.port.RepositorioAcoes;
import com.example.aiservice.domain.AcaoAgente;
import com.example.aiservice.domain.AcessoNegadoException;
import com.example.aiservice.domain.CursoCatalogo;
import com.example.aiservice.domain.Solicitante;
import com.example.aiservice.domain.StatusAcao;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Casos de uso das acoes com as portas mockadas (sem banco e sem Core). */
class AcoesServiceTest {

    private final Solicitante ana = new Solicitante("ana", List.of("COLABORADOR"), 1L);
    private final Solicitante carlos = new Solicitante("carlos", List.of("COLABORADOR"), 2L);
    private final Solicitante admin = new Solicitante("admin", List.of("ADMIN"), null);
    private final CursoCatalogo docker = new CursoCatalogo(1L, "Docker na Prática", "DEVOPS", "INTERMEDIARIO", 20);
    private final Instant agora = Instant.parse("2026-11-27T12:00:00Z");

    private RepositorioAcoes repositorio;
    private CoreGateway core;
    private AcoesService service;

    @BeforeEach
    void setUp() {
        repositorio = mock(RepositorioAcoes.class);
        core = mock(CoreGateway.class);
        service = new AcoesService(repositorio, core, Clock.fixed(agora, ZoneOffset.UTC));
        AtomicLong ids = new AtomicLong(1);
        when(repositorio.salvar(any())).thenAnswer(inv -> {
            AcaoAgente a = inv.getArgument(0);
            if (a.getId() == null) {
                a.definirId(ids.getAndIncrement());
            }
            return a;
        });
    }

    @Test
    void propostaDeInscricaoFicaAguardandoENuncaExecuta() {
        when(core.buscarCurso(ana, 1L)).thenReturn(Optional.of(docker));

        AcaoAgente acao = service.proporInscricao(ana, 1L, "base de containers");

        assertThat(acao.getStatus()).isEqualTo(StatusAcao.AGUARDANDO_AUTORIZACAO);
        assertThat(acao.getResultado()).isNull();
        assertThat(acao.getCriadoEm()).isEqualTo(agora);
    }

    @Test
    void inscricaoEmCursoForaDoCatalogoERejeitada() {
        when(core.buscarCurso(eq(ana), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.proporInscricao(ana, 99L, "x"))
                .isInstanceOf(NaoEncontradaException.class)
                .hasMessageContaining("não existe no catálogo");
        verify(repositorio, never()).salvar(any());
    }

    @Test
    void colaboradorAutorizandoAcaoDeOutroRecebe403ENadaESalvo() {
        AcaoAgente daAna = AcaoAgente.proporInscricao(ana, docker, "x", agora);
        daAna.definirId(7L);
        when(repositorio.buscar(7L)).thenReturn(Optional.of(daAna));

        assertThatThrownBy(() -> service.autorizar(7L, carlos)).isInstanceOf(AcessoNegadoException.class);
        verify(repositorio, never()).salvar(any());
        assertThat(daAna.getStatus()).isEqualTo(StatusAcao.AGUARDANDO_AUTORIZACAO);
    }

    @Test
    void autorizarExecutaComAuditoria() {
        AcaoAgente daAna = AcaoAgente.proporInscricao(ana, docker, "x", agora);
        daAna.definirId(7L);
        when(repositorio.buscar(7L)).thenReturn(Optional.of(daAna));

        AcaoAgente acao = service.autorizar(7L, ana);

        assertThat(acao.getStatus()).isEqualTo(StatusAcao.EXECUTADA);
        assertThat(acao.getAutorizadoPor()).isEqualTo("ana");
        assertThat(acao.getAutorizadoEm()).isEqualTo(agora);
    }

    @Test
    void acaoNegadaNaoExecuta() {
        AcaoAgente daAna = AcaoAgente.proporInscricao(ana, docker, "x", agora);
        daAna.definirId(7L);
        when(repositorio.buscar(7L)).thenReturn(Optional.of(daAna));

        AcaoAgente acao = service.negar(7L, ana);

        assertThat(acao.getStatus()).isEqualTo(StatusAcao.CANCELADA);
        assertThat(acao.getResultado()).doesNotContain("plano de estudos");
    }

    @Test
    void acaoInexistenteDa404() {
        when(repositorio.buscar(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.autorizar(42L, ana)).isInstanceOf(NaoEncontradaException.class);
    }

    @Test
    void colaboradorListaSoAsSuasEAdminListaTodas() {
        service.listar(ana);
        service.listar(admin);

        verify(repositorio).listarDoUsuario("ana");
        verify(repositorio).listarTodas();
    }
}
