package com.example.aiservice.adapters.out.vectorstore;

import com.example.aiservice.application.port.ModeloLinguagem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Ingestao dos documentos institucionais, na subida do servico (secao 8.1 do plano).
 *
 * - Sem chave de LLM: nao faz nada. O servico sobe e as rotas de IA respondem 503.
 * - Arquivo /data/vectorstore.json ja existe: so carrega (nao paga embedding de novo).
 * - Senao: le resources/docs/*.md, quebra em pedacos, gera os embeddings e salva o arquivo.
 *
 * Uma falha aqui nunca derruba o servico: fica registrada no log e as rotas respondem 503.
 */
@Component
class IngestaoDocumentos implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IngestaoDocumentos.class);

    static final String PADRAO_DOCUMENTOS = "classpath:docs/*.md";

    private final SimpleVectorStore vectorStore;
    private final VectorStoreBaseConhecimento baseConhecimento;
    private final ModeloLinguagem modeloLinguagem;
    private final File arquivo;

    IngestaoDocumentos(SimpleVectorStore vectorStore,
                       VectorStoreBaseConhecimento baseConhecimento,
                       ModeloLinguagem modeloLinguagem,
                       @Value("${ia.rag.arquivo-vectorstore}") String arquivo) {
        this.vectorStore = vectorStore;
        this.baseConhecimento = baseConhecimento;
        this.modeloLinguagem = modeloLinguagem;
        this.arquivo = new File(arquivo);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!modeloLinguagem.configurado()) {
            log.warn("OPENAI_API_KEY nao definida: ingestao do RAG pulada. As rotas /api/ia/** vao responder 503.");
            return;
        }
        try {
            if (arquivo.exists()) {
                vectorStore.load(arquivo);
                log.info("RAG: vector store carregado de {} (sem gerar embeddings de novo).", arquivo.getAbsolutePath());
            } else {
                ingerir();
            }
            baseConhecimento.marcarPronta();
        } catch (Exception e) {
            log.error("RAG: falha na ingestao dos documentos ({}). As rotas de IA vao responder 503.", e.getMessage(), e);
        }
    }

    private void ingerir() throws Exception {
        Resource[] recursos = new PathMatchingResourcePatternResolver().getResources(PADRAO_DOCUMENTOS);
        List<Document> documentos = new ArrayList<>();
        for (Resource recurso : recursos) {
            TextReader leitor = new TextReader(recurso);
            leitor.getCustomMetadata().put(VectorStoreBaseConhecimento.METADADO_FONTE, recurso.getFilename());
            documentos.addAll(leitor.get());
        }

        List<Document> pedacos = TokenTextSplitter.builder().withChunkSize(300).build().apply(documentos);
        vectorStore.add(pedacos);

        File pasta = arquivo.getAbsoluteFile().getParentFile();
        if (pasta != null && !pasta.exists() && !pasta.mkdirs()) {
            throw new IllegalStateException("nao consegui criar a pasta " + pasta);
        }
        vectorStore.save(arquivo);
        log.info("RAG: {} documento(s) quebrados em {} pedaco(s), embeddings salvos em {}.",
                recursos.length, pedacos.size(), arquivo.getAbsolutePath());
    }
}
