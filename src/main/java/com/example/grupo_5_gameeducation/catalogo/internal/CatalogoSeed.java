package com.example.grupo_5_gameeducation.catalogo.internal;

import com.example.grupo_5_gameeducation.catalogo.api.Area;
import com.example.grupo_5_gameeducation.catalogo.api.Nivel;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seed do catalogo: so roda com a tabela vazia, entao reiniciar o container nao duplica nada.
 *
 * Os titulos sao os mesmos dos documentos do RAG (ai-service) e incluem os 3 cursos dos
 * cenarios BDD da AC1, para a US1 continuar funcionando com a validacao pelo catalogo.
 */
@Component
@Order(1)
class CatalogoSeed implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogoSeed.class);

    record CursoInicial(String titulo, Area area, Nivel nivel, int cargaHoraria) {
    }

    static final List<CursoInicial> CURSOS = List.of(
            new CursoInicial("Docker na Prática", Area.DEVOPS, Nivel.INTERMEDIARIO, 20),
            new CursoInicial("Introdução a DevOps", Area.DEVOPS, Nivel.INICIANTE, 12),
            new CursoInicial("Kubernetes para Produção", Area.DEVOPS, Nivel.AVANCADO, 40),
            new CursoInicial("Fundamentos de Agile Testing", Area.QA, Nivel.INICIANTE, 16),
            new CursoInicial("Qualidade de Software", Area.QA, Nivel.INTERMEDIARIO, 24),
            new CursoInicial("Java com Spring Boot", Area.DESENVOLVIMENTO, Nivel.INTERMEDIARIO, 40),
            new CursoInicial("Arquitetura de Software e Microsserviços", Area.DESENVOLVIMENTO, Nivel.ESPECIALISTA, 36),
            new CursoInicial("SQL e Modelagem de Dados", Area.DADOS, Nivel.INICIANTE, 20),
            new CursoInicial("Machine Learning Aplicado", Area.DADOS, Nivel.AVANCADO, 36),
            new CursoInicial("Liderança de Equipes Técnicas", Area.GESTAO, Nivel.INTERMEDIARIO, 16));

    private final CursoCatalogoRepository repository;

    CatalogoSeed(CursoCatalogoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            log.info("Seed do catalogo ignorado: a tabela ja tem cursos.");
            return;
        }
        CURSOS.forEach(c -> repository.save(new CursoCatalogoEntity(c.titulo(), c.area(), c.nivel(), c.cargaHoraria())));
        log.info("Seed do catalogo: {} cursos criados.", CURSOS.size());
    }
}
