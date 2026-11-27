package com.example.aiservice.adapters.in.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiservice.application.AcoesService;
import com.example.aiservice.application.port.BaseConhecimento;
import com.example.aiservice.application.port.CoreGateway;
import com.example.aiservice.domain.AcaoAgente;
import com.example.aiservice.domain.CursoCatalogo;
import com.example.aiservice.domain.Solicitante;
import com.example.aiservice.domain.StatusAcao;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.Tool;

/** Tools do agente: presas ao usuario logado, e as de acao so propoem. */
class FerramentasOrientacaoTest {

    private final Solicitante ana = new Solicitante("ana", List.of("COLABORADOR"), 1L);
    private final CursoCatalogo docker = new CursoCatalogo(1L, "Docker na Prática", "DEVOPS", "INTERMEDIARIO", 20);

    private CoreGateway core;
    private AcoesService acoes;
    private FerramentasOrientacao ferramentas;

    @BeforeEach
    void setUp() {
        core = mock(CoreGateway.class);
        acoes = mock(AcoesService.class);
        ferramentas = new FerramentasOrientacao(ana, core, mock(BaseConhecimento.class), acoes);
    }

    @Test
    void nenhumaToolDeixaOLlmEscolherOColaborador() {
        List<String> parametros = Arrays.stream(FerramentasOrientacao.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Tool.class))
                .map(Method::getParameters)
                .flatMap(Arrays::stream)
                .map(Parameter::getName)
                .toList();

        assertThat(parametros).noneMatch(nome -> nome.toLowerCase().contains("colaborador"));
    }

    @Test
    void consultarPerfilUsaSempreOColaboradorDoHeader() {
        ferramentas.consultarPerfil();

        verify(core).buscarPerfil(ana, 1L);
        assertThat(ferramentas.toolsUsadas()).containsExactly("consultarPerfil");
    }

    @Test
    void toolDeAcaoSoPropoeENuncaAutoriza() {
        AcaoAgente proposta = AcaoAgente.proporInscricao(ana, docker, "x", Instant.now());
        proposta.definirId(5L);
        when(acoes.proporInscricao(ana, 1L, "base de containers")).thenReturn(proposta);

        String retorno = ferramentas.proporInscricao(1L, "base de containers");

        assertThat(retorno).contains("aguardando autorização").contains("Nada foi executado");
        assertThat(ferramentas.acoesPropostas()).extracting(AcaoAgente::getStatus)
                .containsExactly(StatusAcao.AGUARDANDO_AUTORIZACAO);
        verify(acoes, never()).autorizar(anyLong(), any());
    }

    @Test
    void listarCursosNormalizaAArea() {
        ferramentas.listarCursos(" devops ");

        verify(core).listarCursos(ana, "DEVOPS");
    }
}
