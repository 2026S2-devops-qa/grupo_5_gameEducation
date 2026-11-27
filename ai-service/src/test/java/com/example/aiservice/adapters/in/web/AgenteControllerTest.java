package com.example.aiservice.adapters.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.aiservice.application.AcoesService;
import com.example.aiservice.application.NaoEncontradaException;
import com.example.aiservice.application.OrientacaoService;
import com.example.aiservice.domain.AcaoAgente;
import com.example.aiservice.domain.AcaoJaDecididaException;
import com.example.aiservice.domain.AcessoNegadoException;
import com.example.aiservice.domain.CursoCatalogo;
import com.example.aiservice.domain.PlanoDeAcao;
import com.example.aiservice.domain.Solicitante;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Contrato REST do RF04 (secao 5.4) e os codigos de erro 403, 404 e 409. */
@WebMvcTest(AgenteController.class)
class AgenteControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrientacaoService orientacaoService;

    @MockitoBean
    AcoesService acoesService;

    private final Solicitante ana = new Solicitante("ana", List.of("COLABORADOR"), 1L);

    private static MockHttpServletRequestBuilder como(MockHttpServletRequestBuilder req, String login, String papel, String colaborador) {
        req.header("X-User-Id", login).header("X-User-Roles", papel);
        return colaborador == null ? req : req.header("X-Colaborador-Id", colaborador);
    }

    private AcaoAgente acaoDaAna() {
        AcaoAgente acao = AcaoAgente.proporInscricao(ana,
                new CursoCatalogo(1L, "Docker na Prática", "DEVOPS", "INTERMEDIARIO", 20), "containers",
                Instant.parse("2026-11-27T12:00:00Z"));
        acao.definirId(1L);
        return acao;
    }

    @Test
    void orientacaoDevolvePlanoComAcoesAguardando() throws Exception {
        when(orientacaoService.orientar(eq("montar meu plano do trimestre"), eq(ana)))
                .thenReturn(new PlanoDeAcao("Plano proposto.", List.of(acaoDaAna()), List.of("consultarPerfil", "proporInscricao")));

        mockMvc.perform(como(post("/api/ia/agentes/orientacao"), "ana", "COLABORADOR", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objetivo\":\"montar meu plano do trimestre\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo").value("Plano proposto."))
                .andExpect(jsonPath("$.acoes[0].id").value(1))
                .andExpect(jsonPath("$.acoes[0].tipo").value("INSCRICAO"))
                .andExpect(jsonPath("$.acoes[0].status").value("AGUARDANDO_AUTORIZACAO"))
                .andExpect(jsonPath("$.acoes[0].autorizadoPor").doesNotExist())
                .andExpect(jsonPath("$.toolsUsadas[1]").value("proporInscricao"));
    }

    @Test
    void autorizarAcaoDeOutroUsuarioDa403() throws Exception {
        when(acoesService.autorizar(eq(1L), any()))
                .thenThrow(new AcessoNegadoException("Você só pode autorizar ou negar as suas próprias ações."));

        mockMvc.perform(como(post("/api/ia/agentes/acoes/1/autorizar"), "carlos", "COLABORADOR", "2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Você só pode autorizar ou negar as suas próprias ações."));
    }

    @Test
    void acaoInexistenteDa404() throws Exception {
        when(acoesService.negar(eq(9L), any())).thenThrow(new NaoEncontradaException("Ação 9 não encontrada."));

        mockMvc.perform(como(post("/api/ia/agentes/acoes/9/negar"), "ana", "COLABORADOR", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void acaoJaDecididaDa409() throws Exception {
        when(acoesService.autorizar(eq(1L), any())).thenThrow(new AcaoJaDecididaException("Esta ação já foi decidida."));

        mockMvc.perform(como(post("/api/ia/agentes/acoes/1/autorizar"), "ana", "COLABORADOR", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    void adminPedindoOrientacaoRecebe400() throws Exception {
        when(orientacaoService.orientar(any(), any()))
                .thenThrow(new IllegalArgumentException("A orientação é para colaboradores."));

        mockMvc.perform(como(post("/api/ia/agentes/orientacao"), "admin", "ADMIN", null)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"objetivo\":\"plano\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaAsAcoesDoUsuario() throws Exception {
        when(acoesService.listar(ana)).thenReturn(List.of(acaoDaAna()));

        mockMvc.perform(como(get("/api/ia/agentes/acoes"), "ana", "COLABORADOR", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descricao").value("Inscrever no curso Docker na Prática"));
    }

    @Test
    void semIdentidadeDa401() throws Exception {
        mockMvc.perform(get("/api/ia/agentes/acoes")).andExpect(status().isUnauthorized());
    }
}
