package com.example.aiservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Os documentos do RAG precisam citar os cursos com os MESMOS titulos do seed do catalogo
 * (CatalogoSeed do Core, RF01). Senao o RAG recomenda curso que nao existe na plataforma.
 */
class DocumentosInstitucionaisTest {

    /** Titulos do seed do Core (catalogo/internal/CatalogoSeed.java, RF01 do Bruno). */
    static final List<String> TITULOS_DO_SEED = List.of(
            "Docker na Prática",
            "Introdução a DevOps",
            "Kubernetes para Produção",
            "Fundamentos de Agile Testing",
            "Qualidade de Software",
            "Java com Spring Boot",
            "Arquitetura de Software e Microsserviços",
            "SQL e Modelagem de Dados",
            "Machine Learning Aplicado",
            "Liderança de Equipes Técnicas");

    @Test
    void existemDeQuatroASeisDocumentos() throws IOException {
        assertThat(documentos()).hasSizeBetween(4, 6);
    }

    @Test
    void catalogoCitaTodosOsCursosDoSeedComOTituloExato() throws IOException {
        String catalogo = ler(new PathMatchingResourcePatternResolver().getResource("classpath:docs/catalogo-cursos.md"));
        assertThat(catalogo).contains(TITULOS_DO_SEED);
    }

    @Test
    void trilhasCitamTodosOsCursosDoSeedComOTituloExato() throws IOException {
        String trilhas = ler(new PathMatchingResourcePatternResolver().getResource("classpath:docs/politica-de-trilhas.md"));
        assertThat(trilhas).contains(TITULOS_DO_SEED);
    }

    private static Resource[] documentos() throws IOException {
        return new PathMatchingResourcePatternResolver().getResources("classpath:docs/*.md");
    }

    private static String ler(Resource recurso) throws IOException {
        return recurso.getContentAsString(StandardCharsets.UTF_8);
    }
}
