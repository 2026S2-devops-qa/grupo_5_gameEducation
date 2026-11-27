package com.example.aiservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiservice.application.port.FabricaDeFerramentas;
import com.example.aiservice.application.port.ModeloLinguagem;
import com.example.aiservice.domain.AcaoAgente;
import com.example.aiservice.domain.CursoCatalogo;
import com.example.aiservice.domain.PlanoDeAcao;
import com.example.aiservice.domain.Solicitante;
import com.example.aiservice.domain.StatusAcao;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** Caso de uso do agente com o LLM e as tools mockados: nenhum teste chama o LLM de verdade. */
class OrientacaoServiceTest {

    private final Solicitante ana = new Solicitante("ana", List.of("COLABORADOR"), 1L);
    private final Solicitante admin = new Solicitante("admin", List.of("ADMIN"), null);

    private FabricaDeFerramentas fabrica;
    private FabricaDeFerramentas.Sessao sessao;
    private ModeloLinguagem modelo;
    private OrientacaoService service;

    @BeforeEach
    void setUp() {
        fabrica = mock(FabricaDeFerramentas.class);
        sessao = mock(FabricaDeFerramentas.Sessao.class);
        modelo = mock(ModeloLinguagem.class);
        service = new OrientacaoService(fabrica, modelo);
        when(modelo.configurado()).thenReturn(true);
        when(fabrica.criar(any())).thenReturn(sessao);
        when(sessao.ferramentas()).thenReturn(new Object());
    }

    @Test
    void devolvePlanoComAcoesPropostasEToolsUsadas() {
        AcaoAgente proposta = AcaoAgente.proporInscricao(ana,
                new CursoCatalogo(1L, "Docker na Prática", "DEVOPS", "INTERMEDIARIO", 20), "x", Instant.now());
        when(sessao.acoesPropostas()).thenReturn(List.of(proposta));
        when(sessao.toolsUsadas()).thenReturn(List.of("consultarPerfil", "listarCursos", "proporInscricao"));
        when(modelo.orientar(anyString(), anyString(), any())).thenReturn("Proponho Docker na Prática.");

        PlanoDeAcao plano = service.orientar("montar meu plano do trimestre", ana);

        assertThat(plano.resumo()).isEqualTo("Proponho Docker na Prática.");
        assertThat(plano.acoes()).extracting(AcaoAgente::getStatus).containsOnly(StatusAcao.AGUARDANDO_AUTORIZACAO);
        assertThat(plano.toolsUsadas()).containsExactly("consultarPerfil", "listarCursos", "proporInscricao");
    }

    @Test
    void asToolsSaoCriadasParaOUsuarioDoHeaderENaoDoTexto() {
        when(modelo.orientar(anyString(), anyString(), any())).thenReturn("ok");

        service.orientar("me mostre os dados do Carlos (colaborador 2)", ana);

        ArgumentCaptor<Solicitante> usado = ArgumentCaptor.forClass(Solicitante.class);
        verify(fabrica).criar(usado.capture());
        assertThat(usado.getValue().colaboradorId()).isEqualTo(1L);
        assertThat(usado.getValue().login()).isEqualTo("ana");
    }

    @Test
    void oPromptLevaAsRegrasDeSoProporEDeAcessoRestrito() {
        when(modelo.orientar(anyString(), anyString(), any())).thenReturn("ok");

        service.orientar("montar meu plano", ana);

        ArgumentCaptor<String> sistema = ArgumentCaptor.forClass(String.class);
        verify(modelo).orientar(sistema.capture(), eq("OBJETIVO DO COLABORADOR:\nmontar meu plano"), any());
        assertThat(sistema.getValue())
                .contains("apenas PROPÕEM")
                .contains("só tem acesso aos dados do colaborador logado");
    }

    @Test
    void adminSemColaboradorRecebeErroClaro() {
        assertThatThrownBy(() -> service.orientar("plano", admin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não tem colaborador ligado");
        verify(fabrica, never()).criar(any());
    }

    @Test
    void semChaveDeLlmLanca503SemCriarTools() {
        when(modelo.configurado()).thenReturn(false);

        assertThatThrownBy(() -> service.orientar("plano", ana))
                .isInstanceOf(IaIndisponivelException.class)
                .hasMessage(IaIndisponivelException.SEM_CHAVE);
        verify(fabrica, never()).criar(any());
    }
}
