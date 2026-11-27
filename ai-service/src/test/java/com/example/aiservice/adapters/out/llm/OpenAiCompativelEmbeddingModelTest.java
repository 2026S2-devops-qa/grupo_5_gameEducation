package com.example.aiservice.adapters.out.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Adaptador de embeddings com servidor HTTP simulado: nao chama o provedor de verdade. */
class OpenAiCompativelEmbeddingModelTest {

    private static final String BASE = "https://generativelanguage.googleapis.com/v1beta/openai";

    @Test
    void aceitaRespostaSemUsageComoADoGeminiEMantemAOrdemPeloIndex() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        OpenAiCompativelEmbeddingModel modelo =
                new OpenAiCompativelEmbeddingModel(builder, BASE, "chave-de-teste", "gemini-embedding-001");

        servidor.expect(requestTo(BASE + "/embeddings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer chave-de-teste"))
                .andExpect(content().json("{\"model\":\"gemini-embedding-001\",\"input\":[\"a\",\"b\"]}"))
                // sem o campo "usage", e fora de ordem
                .andRespond(withSuccess("""
                        {"object":"list","data":[
                          {"object":"embedding","index":1,"embedding":[0.3,0.4]},
                          {"object":"embedding","index":0,"embedding":[0.1,0.2]}
                        ],"model":"gemini-embedding-001"}""", MediaType.APPLICATION_JSON));

        EmbeddingResponse resposta = modelo.call(new EmbeddingRequest(List.of("a", "b"), null));

        assertThat(resposta.getResults()).hasSize(2);
        assertThat(resposta.getResults().get(0).getOutput()).containsExactly(0.1f, 0.2f);
        assertThat(resposta.getResults().get(1).getOutput()).containsExactly(0.3f, 0.4f);
        assertThat(modelo.dimensions()).isEqualTo(2);
        servidor.verify();
    }

    @Test
    void aceitaRespostaSemIndexESemUsageNoFormatoRealDoGemini() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        OpenAiCompativelEmbeddingModel modelo =
                new OpenAiCompativelEmbeddingModel(builder, BASE, "chave-de-teste", "gemini-embedding-001");

        servidor.expect(requestTo(BASE + "/embeddings"))
                .andRespond(withSuccess("""
                        {"object":"list","data":[
                          {"object":"embedding","embedding":[0.1,0.2]},
                          {"object":"embedding","embedding":[0.3,0.4]}
                        ],"model":"gemini-embedding-001"}""", MediaType.APPLICATION_JSON));

        EmbeddingResponse resposta = modelo.call(new EmbeddingRequest(List.of("a", "b"), null));

        assertThat(resposta.getResults().get(0).getOutput()).containsExactly(0.1f, 0.2f);
        assertThat(resposta.getResults().get(1).getOutput()).containsExactly(0.3f, 0.4f);
        assertThat(resposta.getResults().get(1).getIndex()).isEqualTo(1);
    }
}
