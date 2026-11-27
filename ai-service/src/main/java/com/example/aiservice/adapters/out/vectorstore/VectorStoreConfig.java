package com.example.aiservice.adapters.out.vectorstore;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SimpleVectorStore: vetores em memoria, salvos num arquivo JSON no volume /data.
 * Trade-off (RNF05): para 5 documentos nao vale subir mais um banco. Com milhares de
 * documentos, troca-se este adaptador por pgvector sem mexer no nucleo.
 */
@Configuration
class VectorStoreConfig {

    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
