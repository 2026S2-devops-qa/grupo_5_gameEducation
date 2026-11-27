package com.example.aiservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
