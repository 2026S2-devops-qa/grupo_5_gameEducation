package com.example.grupo_5_gameeducation.colaboradores.internal;

import com.example.grupo_5_gameeducation.catalogo.api.Area;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seed dos colaboradores: so roda com a tabela vazia (reiniciar o container nao duplica).
 *
 * A ORDEM importa: os ids precisam bater com os usuarios fixos do gateway
 * (ana = colaborador 1, carlos = colaborador 2; plano, secao 5.3).
 */
@Component
@Order(2)
class ColaboradoresSeed implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ColaboradoresSeed.class);

    private final ColaboradorRepository repository;

    ColaboradoresSeed(ColaboradorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            log.info("Seed de colaboradores ignorado: a tabela ja tem colaboradores.");
            return;
        }
        ColaboradorEntity ana = repository.save(new ColaboradorEntity(
                "Ana Souza", "ana@empresa.com", "Analista de QA", Area.QA, 3, 4, 8.1));
        ColaboradorEntity carlos = repository.save(new ColaboradorEntity(
                "Carlos Lima", "carlos@empresa.com", "Desenvolvedor Backend", Area.DESENVOLVIMENTO, 5, 6, 7.4));
        repository.save(new ColaboradorEntity(
                "Beatriz Rocha", "beatriz@empresa.com", "Engenheira DevOps", Area.DEVOPS, 8, 12, 9.0));

        if (!Long.valueOf(1L).equals(ana.getId()) || !Long.valueOf(2L).equals(carlos.getId())) {
            log.warn("Seed de colaboradores: ids inesperados (ana={}, carlos={}). Os usuarios do gateway "
                    + "esperam ana=1 e carlos=2. Recrie o banco com 'docker compose down -v'.",
                    ana.getId(), carlos.getId());
        }
        log.info("Seed de colaboradores: 3 colaboradores criados (ana={}, carlos={}).", ana.getId(), carlos.getId());
    }
}
