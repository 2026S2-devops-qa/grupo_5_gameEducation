package com.example.aiservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiservice.application.port.BaseConhecimento;
import com.example.aiservice.application.port.ModeloLinguagem;
import com.example.aiservice.domain.CursoRecomendado;
import com.example.aiservice.domain.Recomendacao;
import com.example.aiservice.domain.RespostaGerada;
import com.example.aiservice.domain.Solicitante;
import com.example.aiservice.domain.Trecho;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Teste de unidade do caso de uso do RF03 com as duas portas mockadas:
 * nenhum teste chama o LLM de verdade nem gera embedding (nao gasta chamada paga no mvn verify).
 */
class RecomendacaoServiceTest {

    private final Solicitante ana = new Solicitante("ana", List.of("COLABORADOR"), 1L);

    private BaseConhecimento base;
    private ModeloLinguagem modelo;
    private RecomendacaoService service;

    private final Trecho trilhaDevops = new Trecho("politica-de-trilhas.md",
            "Trilha de DevOps: Introdução a DevOps, Docker na Prática e Kubernetes para Produção.", 0.82);

    @BeforeEach
    void setUp() {
        base = mock(BaseConhecimento.class);
        modelo = mock(ModeloLinguagem.class);
        service = new RecomendacaoService(base, modelo);
        when(modelo.configurado()).thenReturn(true);
        when(base.pronta()).thenReturn(true);
    }

    @Test
    void recomendaComBaseNosTrechosECitaAsFontes() {
        when(base.buscar("quero crescer em DevOps", RecomendacaoService.TOP_K)).thenReturn(List.of(trilhaDevops));
        when(modelo.gerar(anyString(), anyString())).thenReturn(new RespostaGerada(
                "Siga a trilha de DevOps.",
                List.of(new CursoRecomendado("Docker na Prática", "containers"))));

        Recomendacao recomendacao = service.recomendar("quero crescer em DevOps", ana);

        assertThat(recomendacao.resposta()).isEqualTo("Siga a trilha de DevOps.");
        assertThat(recomendacao.cursos()).extracting(CursoRecomendado::titulo).containsExactly("Docker na Prática");
        assertThat(recomendacao.fontes()).containsExactly("politica-de-trilhas.md");
    }

    @Test
    void oPromptLevaOsTrechosEOObjetivoEAsRegrasContraAlucinacao() {
        when(base.buscar(anyString(), anyInt())).thenReturn(List.of(trilhaDevops));
        when(modelo.gerar(anyString(), anyString())).thenReturn(new RespostaGerada("ok", List.of()));

        service.recomendar("quero crescer em DevOps", ana);

        ArgumentCaptor<String> sistema = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usuario = ArgumentCaptor.forClass(String.class);
        verify(modelo).gerar(sistema.capture(), usuario.capture());
        assertThat(sistema.getValue()).contains("SOMENTE cursos cujo título aparece nos TRECHOS");
        assertThat(usuario.getValue())
                .contains("[fonte: politica-de-trilhas.md]")
                .contains("Docker na Prática")
                .contains("quero crescer em DevOps");
    }

    @Test
    void cursoInventadoPeloLlmNaoChegaAoUsuario() {
        when(base.buscar(anyString(), anyInt())).thenReturn(List.of(trilhaDevops));
        when(modelo.gerar(anyString(), anyString())).thenReturn(new RespostaGerada("ok",
                List.of(new CursoRecomendado("Terraform Avançado", "não existe no catálogo"))));

        Recomendacao recomendacao = service.recomendar("quero crescer em DevOps", ana);

        assertThat(recomendacao.cursos()).isEmpty();
    }

    @Test
    void perguntaForaDoAssuntoRespondeQueNaoEncontrouSemChamarOLlm() {
        when(base.buscar(anyString(), anyInt())).thenReturn(List.of());

        Recomendacao recomendacao = service.recomendar("qual a receita de bolo de cenoura?", ana);

        assertThat(recomendacao.resposta()).isEqualTo(Recomendacao.SEM_BASE);
        assertThat(recomendacao.cursos()).isEmpty();
        assertThat(recomendacao.fontes()).isEmpty();
        verify(modelo, never()).gerar(anyString(), anyString());
    }

    @Test
    void semChaveDeLlmLanca503SemBuscarNemGerar() {
        when(modelo.configurado()).thenReturn(false);

        assertThatThrownBy(() -> service.recomendar("quero crescer em DevOps", ana))
                .isInstanceOf(IaIndisponivelException.class)
                .hasMessage("LLM não configurado: defina OPENAI_API_KEY no .env");
        verify(base, never()).buscar(anyString(), anyInt());
        verify(modelo, never()).gerar(anyString(), anyString());
    }

    @Test
    void baseAindaNaoProntaLanca503() {
        when(base.pronta()).thenReturn(false);

        assertThatThrownBy(() -> service.recomendar("quero crescer em DevOps", ana))
                .isInstanceOf(IaIndisponivelException.class)
                .hasMessage(IaIndisponivelException.BASE_NAO_PRONTA);
    }

    @Test
    void objetivoEmBrancoERejeitado() {
        assertThatThrownBy(() -> service.recomendar("  ", ana)).isInstanceOf(IllegalArgumentException.class);
        verify(base, never()).buscar(anyString(), eq(RecomendacaoService.TOP_K));
    }
}
