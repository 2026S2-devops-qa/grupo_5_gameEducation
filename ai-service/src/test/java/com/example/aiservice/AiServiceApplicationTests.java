package com.example.aiservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.aiservice.adapters.in.tools.FerramentasOrientacao;
import com.example.aiservice.application.port.FabricaDeFerramentas;
import com.example.aiservice.domain.Solicitante;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Caso exigido pelo plano (secao 5.6) e pelo checklist: SEM chave de LLM o servico sobe,
 * o health fica UP e a rota de IA responde 503 com mensagem clara. Nao chama o LLM.
 */
@SpringBootTest(properties = {
        "spring.ai.openai.api-key=",
        "ia.rag.arquivo-vectorstore=target/vectorstore-teste.json"})
@AutoConfigureMockMvc
class AiServiceApplicationTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void semChaveOServicoSobeEOHealthFicaUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void semChaveARecomendacaoResponde503ComMensagemClara() throws Exception {
        mockMvc.perform(post("/api/ia/recomendacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "ana")
                        .header("X-User-Roles", "COLABORADOR")
                        .header("X-Colaborador-Id", "1")
                        .content("{\"objetivo\":\"quero crescer em DevOps\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erro").value("LLM não configurado: defina OPENAI_API_KEY no .env"));
    }

    @Autowired
    FabricaDeFerramentas fabricaDeFerramentas;

    /**
     * Fluxo completo do RF04 com JPA (H2) e o StubCoreGateway, sem LLM: a tool PROPOE, a acao fica
     * aguardando, outro colaborador leva 403, o dono autoriza e a auditoria fica registrada.
     */
    @Test
    void fluxoDeAutorizacaoDoAgenteDePontaAPonta() throws Exception {
        Solicitante ana = new Solicitante("ana", List.of("COLABORADOR"), 1L);
        FerramentasOrientacao tools = (FerramentasOrientacao) fabricaDeFerramentas.criar(ana).ferramentas();
        tools.proporInscricao(1L, "base de containers");
        Long id = tools.acoesPropostas().get(0).getId();

        mockMvc.perform(get("/api/ia/agentes/acoes")
                        .header("X-User-Id", "ana").header("X-User-Roles", "COLABORADOR").header("X-Colaborador-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].status").value("AGUARDANDO_AUTORIZACAO"));

        mockMvc.perform(post("/api/ia/agentes/acoes/" + id + "/autorizar")
                        .header("X-User-Id", "carlos").header("X-User-Roles", "COLABORADOR").header("X-Colaborador-Id", "2"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/ia/agentes/acoes/" + id + "/autorizar")
                        .header("X-User-Id", "ana").header("X-User-Roles", "COLABORADOR").header("X-Colaborador-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUTADA"))
                .andExpect(jsonPath("$.autorizadoPor").value("ana"))
                .andExpect(jsonPath("$.autorizadoEm").exists())
                .andExpect(jsonPath("$.resultado").value("Registrado no plano de estudos: Docker na Prática."));

        mockMvc.perform(post("/api/ia/agentes/acoes/" + id + "/negar")
                        .header("X-User-Id", "ana").header("X-User-Roles", "COLABORADOR").header("X-Colaborador-Id", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    void semChaveAOrientacaoResponde503() throws Exception {
        mockMvc.perform(post("/api/ia/agentes/orientacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "ana").header("X-User-Roles", "COLABORADOR").header("X-Colaborador-Id", "1")
                        .content("{\"objetivo\":\"montar meu plano do trimestre\"}"))
                .andExpect(status().isServiceUnavailable());
    }
}
