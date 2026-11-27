package com.example.aiservice.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.aiservice.application.IaIndisponivelException;
import com.example.aiservice.application.RecomendacaoService;
import com.example.aiservice.domain.CursoRecomendado;
import com.example.aiservice.domain.Recomendacao;
import com.example.aiservice.domain.Solicitante;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Contrato REST do RF03 (secao 5.4): formato da resposta, headers de identidade e erros {"erro"}. */
@WebMvcTest(RecomendacaoController.class)
class RecomendacaoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RecomendacaoService recomendacaoService;

    private static MockHttpServletRequestBuilder comoAna(String corpo) {
        return post("/api/ia/recomendacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", "ana")
                .header("X-User-Roles", "COLABORADOR")
                .header("X-Colaborador-Id", "1")
                .content(corpo);
    }

    @Test
    void devolveRespostaCursosEFontes() throws Exception {
        when(recomendacaoService.recomendar(eq("quero crescer em DevOps"), any())).thenReturn(new Recomendacao(
                "Siga a trilha de DevOps.",
                List.of(new CursoRecomendado("Docker na Prática", "containers")),
                List.of("politica-de-trilhas.md")));

        mockMvc.perform(comoAna("{\"objetivo\":\"quero crescer em DevOps\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resposta").value("Siga a trilha de DevOps."))
                .andExpect(jsonPath("$.cursos[0].titulo").value("Docker na Prática"))
                .andExpect(jsonPath("$.cursos[0].motivo").value("containers"))
                .andExpect(jsonPath("$.fontes[0]").value("politica-de-trilhas.md"));
    }

    @Test
    void repassaAIdentidadeDosHeadersParaOCasoDeUso() throws Exception {
        when(recomendacaoService.recomendar(any(), any())).thenReturn(Recomendacao.semBase());

        mockMvc.perform(comoAna("{\"objetivo\":\"x\"}")).andExpect(status().isOk());

        verify(recomendacaoService).recomendar("x", new Solicitante("ana", List.of("COLABORADOR"), 1L));
    }

    @Test
    void semXUserIdResponde401() throws Exception {
        mockMvc.perform(post("/api/ia/recomendacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objetivo\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").exists());
        verify(recomendacaoService, never()).recomendar(any(), any());
    }

    @Test
    void objetivoEmBrancoResponde400() throws Exception {
        mockMvc.perform(comoAna("{\"objetivo\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Informe o objetivo."));
    }

    @Test
    void iaIndisponivelResponde503ComMensagem() throws Exception {
        when(recomendacaoService.recomendar(any(), any()))
                .thenThrow(new IaIndisponivelException(IaIndisponivelException.SEM_CHAVE));

        mockMvc.perform(comoAna("{\"objetivo\":\"quero crescer em DevOps\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erro").value("LLM não configurado: defina OPENAI_API_KEY no .env"));
    }
}
