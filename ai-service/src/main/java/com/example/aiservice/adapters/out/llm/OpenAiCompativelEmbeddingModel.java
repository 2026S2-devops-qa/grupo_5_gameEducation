package com.example.aiservice.adapters.out.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Embeddings pelo endpoint compativel com a OpenAI ({base}/embeddings), usado por OpenAI, Gemini e Ollama.
 *
 * Por que nao o OpenAiEmbeddingModel do Spring AI: no Spring AI 2.0 ele usa o SDK oficial da OpenAI,
 * que exige o campo "usage" na resposta. O endpoint compativel do Gemini nao manda esse campo, e o SDK
 * rejeita a resposta ("`usage` is not set"). Este adaptador le so os vetores, entao funciona com os tres
 * provedores pelo mesmo .env. O embedding automatico da OpenAI fica desligado por
 * spring.ai.model.embedding=none (application.properties). O chat continua com o ChatClient do Spring AI.
 */
@Component
class OpenAiCompativelEmbeddingModel implements EmbeddingModel {

    /** Quantos textos por chamada (o Gemini aceita no maximo 100 por lote). */
    static final int TAMANHO_LOTE = 50;

    private final RestClient restClient;
    private final String modelo;
    private volatile int dimensoes = -1;

    @Autowired
    OpenAiCompativelEmbeddingModel(@Value("${spring.ai.openai.base-url}") String baseUrl,
                                   @Value("${spring.ai.openai.api-key:}") String apiKey,
                                   @Value("${spring.ai.openai.embedding.model}") String modelo) {
        this(RestClient.builder(), baseUrl, apiKey, modelo);
    }

    OpenAiCompativelEmbeddingModel(RestClient.Builder builder, String baseUrl, String apiKey, String modelo) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.modelo = modelo;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> textos = request.getInstructions();
        List<Embedding> embeddings = new ArrayList<>();
        for (int inicio = 0; inicio < textos.size(); inicio += TAMANHO_LOTE) {
            List<String> lote = textos.subList(inicio, Math.min(inicio + TAMANHO_LOTE, textos.size()));
            RespostaEmbeddings resposta = restClient.post()
                    .uri("/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("model", modelo, "input", lote))
                    .retrieve()
                    .body(RespostaEmbeddings.class);
            if (resposta == null || resposta.data() == null || resposta.data().size() != lote.size()) {
                throw new IllegalStateException("Resposta de embeddings inesperada do provedor de LLM.");
            }
            // A OpenAI manda "index"; o Gemini nao manda (ai vale a posicao na lista, que segue a ordem do input)
            List<ItemEmbedding> itens = new ArrayList<>(resposta.data());
            if (itens.stream().allMatch(i -> i.index() != null)) {
                itens.sort(Comparator.comparingInt(ItemEmbedding::index));
            }
            for (int i = 0; i < itens.size(); i++) {
                embeddings.add(new Embedding(paraFloat(itens.get(i).embedding()), inicio + i));
            }
        }
        if (!embeddings.isEmpty()) {
            dimensoes = embeddings.get(0).getOutput().length;
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return call(new EmbeddingRequest(List.of(getEmbeddingContent(document)), null)).getResult().getOutput();
    }

    /** Guarda o tamanho do vetor depois da primeira chamada, para nao gastar uma chamada extra a cada consulta. */
    @Override
    public int dimensions() {
        if (dimensoes < 0) {
            dimensoes = embed("dimensao").length;
        }
        return dimensoes;
    }

    private static float[] paraFloat(List<Double> valores) {
        float[] vetor = new float[valores.size()];
        for (int i = 0; i < vetor.length; i++) {
            vetor[i] = valores.get(i).floatValue();
        }
        return vetor;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaEmbeddings(List<ItemEmbedding> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ItemEmbedding(Integer index, List<Double> embedding) {
    }
}
