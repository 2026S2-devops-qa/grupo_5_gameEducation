package com.example.aiservice.application.port;

import com.example.aiservice.domain.Trecho;
import java.util.List;

/**
 * Porta de saida: busca semantica nos documentos institucionais (o "R" do RAG).
 * Implementada por VectorStoreBaseConhecimento (Spring AI). Para milhares de documentos,
 * basta trocar o adaptador (ex: pgvector) sem mexer no nucleo.
 */
public interface BaseConhecimento {

    /** Os {@code k} trechos mais parecidos com a pergunta, so os acima da similaridade minima. */
    List<Trecho> buscar(String pergunta, int k);

    /** A ingestao terminou e a base pode ser consultada? */
    boolean pronta();
}
