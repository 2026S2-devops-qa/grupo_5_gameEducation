package com.example.aiservice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Regras do RF04 no dominio: nada executa sem autorizacao, e so o dono (ou ADMIN) decide. */
class AcaoAgenteTest {

    private final Solicitante ana = new Solicitante("ana", List.of("COLABORADOR"), 1L);
    private final Solicitante carlos = new Solicitante("carlos", List.of("COLABORADOR"), 2L);
    private final Solicitante admin = new Solicitante("admin", List.of("ADMIN"), null);
    private final CursoCatalogo docker = new CursoCatalogo(1L, "Docker na Prática", "DEVOPS", "INTERMEDIARIO", 20);
    private final Instant agora = Instant.parse("2026-11-27T12:00:00Z");
    private final Instant depois = Instant.parse("2026-11-27T13:00:00Z");

    @Test
    void propostaNasceAguardandoAutorizacaoESemAuditoria() {
        AcaoAgente acao = AcaoAgente.proporInscricao(ana, docker, "base de containers", agora);

        assertThat(acao.getStatus()).isEqualTo(StatusAcao.AGUARDANDO_AUTORIZACAO);
        assertThat(acao.getColaboradorId()).isEqualTo(1L);
        assertThat(acao.getSolicitadoPor()).isEqualTo("ana");
        assertThat(acao.getCursoId()).isEqualTo(1L);
        assertThat(acao.getAutorizadoPor()).isNull();
        assertThat(acao.getAutorizadoEm()).isNull();
        assertThat(acao.getResultado()).isNull();
    }

    @Test
    void autorizarExecutaERegistraQuemEQuando() {
        AcaoAgente acao = AcaoAgente.proporInscricao(ana, docker, "motivo", agora);

        acao.autorizar(ana, depois);

        assertThat(acao.getStatus()).isEqualTo(StatusAcao.EXECUTADA);
        assertThat(acao.getAutorizadoPor()).isEqualTo("ana");
        assertThat(acao.getAutorizadoEm()).isEqualTo(depois);
        assertThat(acao.getResultado()).contains("plano de estudos").contains("Docker na Prática");
    }

    @Test
    void acompanhamentoAutorizadoViraLembreteComData() {
        AcaoAgente acao = AcaoAgente.proporAcompanhamento(ana, 30, "revisar o plano", agora);

        acao.autorizar(ana, depois);

        assertThat(acao.getResultado()).contains("2026-12-27");
    }

    @Test
    void outroColaboradorNaoPodeAutorizarNemNegar() {
        AcaoAgente acao = AcaoAgente.proporInscricao(ana, docker, "motivo", agora);

        assertThatThrownBy(() -> acao.autorizar(carlos, depois)).isInstanceOf(AcessoNegadoException.class);
        assertThatThrownBy(() -> acao.negar(carlos, depois)).isInstanceOf(AcessoNegadoException.class);
        assertThat(acao.getStatus()).isEqualTo(StatusAcao.AGUARDANDO_AUTORIZACAO);
        assertThat(acao.getAutorizadoPor()).isNull();
    }

    @Test
    void adminPodeDecidirQualquerAcao() {
        AcaoAgente acao = AcaoAgente.proporInscricao(ana, docker, "motivo", agora);

        acao.autorizar(admin, depois);

        assertThat(acao.getStatus()).isEqualTo(StatusAcao.EXECUTADA);
        assertThat(acao.getAutorizadoPor()).isEqualTo("admin");
    }

    @Test
    void negarCancelaSemExecutar() {
        AcaoAgente acao = AcaoAgente.proporInscricao(ana, docker, "motivo", agora);

        acao.negar(ana, depois);

        assertThat(acao.getStatus()).isEqualTo(StatusAcao.CANCELADA);
        assertThat(acao.getAutorizadoPor()).isEqualTo("ana");
        assertThat(acao.getResultado()).doesNotContain("plano de estudos").contains("Nada foi executado");
    }

    @Test
    void acaoJaDecididaNaoPodeSerDecididaDeNovo() {
        AcaoAgente acao = AcaoAgente.proporInscricao(ana, docker, "motivo", agora);
        acao.negar(ana, depois);

        assertThatThrownBy(() -> acao.autorizar(ana, depois)).isInstanceOf(AcaoJaDecididaException.class);
        assertThat(acao.getStatus()).isEqualTo(StatusAcao.CANCELADA);
    }

    @Test
    void usuarioSemColaboradorNaoRecebeProposta() {
        assertThatThrownBy(() -> AcaoAgente.proporInscricao(admin, docker, "motivo", agora))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acompanhamentoForaDoIntervaloERejeitado() {
        assertThatThrownBy(() -> AcaoAgente.proporAcompanhamento(ana, 0, "x", agora))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AcaoAgente.proporAcompanhamento(ana, 181, "x", agora))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
