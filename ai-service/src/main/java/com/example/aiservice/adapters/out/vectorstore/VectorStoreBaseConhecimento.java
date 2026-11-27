package com.example.aiservice.adapters.out.vectorstore;

import com.example.aiservice.application.IaIndisponivelException;
import com.example.aiservice.application.port.BaseConhecimento;
import com.example.aiservice.domain.Trecho;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Adaptador de saida da porta {@link BaseConhecimento} com o vector store do Spring AI. */
@Component
class VectorStoreBaseConhecimento implements BaseConhecimento {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreBaseConhecimento.class);

    static final String METADADO_FONTE = "fonte";

    private final VectorStore vectorStore;
    private final double similaridadeMinima;
    private final AtomicBoolean pronta = new AtomicBoolean(false);

    VectorStoreBaseConhecimento(VectorStore vectorStore,
                                @Value("${ia.rag.similaridade-minima:0.3}") double similaridadeMinima) {
        this.vectorStore = vectorStore;
        this.similaridadeMinima = similaridadeMinima;
    }

    @Override
    public List<Trecho> buscar(String pergunta, int k) {
        try {
            List<Document> documentos = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(pergunta)
                    .topK(k)
                    .similarityThreshold(similaridadeMinima)
                    .build());
            return documentos.stream().map(VectorStoreBaseConhecimento::paraTrecho).toList();
        } catch (RuntimeException e) {
            // a busca gera o embedding da pergunta, ou seja, tambem chama o LLM
            log.error("Falha na busca semantica: {}", e.getMessage());
            throw new IaIndisponivelException(IaIndisponivelException.FALHA_LLM, e);
        }
    }

    @Override
    public boolean pronta() {
        return pronta.get();
    }

    void marcarPronta() {
        pronta.set(true);
    }

    private static Trecho paraTrecho(Document documento) {
        Object fonte = documento.getMetadata().get(METADADO_FONTE);
        double score = documento.getScore() == null ? 0.0 : documento.getScore();
        return new Trecho(fonte == null ? "desconhecida" : fonte.toString(), documento.getText(), score);
    }
}
