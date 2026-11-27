package com.example.aiservice.adapters.out.core;

import com.example.aiservice.application.NaoEncontradaException;
import com.example.aiservice.application.port.CoreGateway;
import com.example.aiservice.domain.AcessoNegadoException;
import com.example.aiservice.domain.CursoCatalogo;
import com.example.aiservice.domain.PerfilColaborador;
import com.example.aiservice.domain.Solicitante;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub da porta {@link CoreGateway}, com os mesmos dados do seed do RF01 (Ana=1, Carlos=2 e os
 * 10 cursos). So existe para desenvolver o RF04 enquanto o RF01 nao esta na branch af.
 * Ativo quando ia.core.stub=true. Na versao final fica DESLIGADO e o RestCoreGateway e usado.
 *
 * Reproduz a regra de acesso do Core: colaborador so ve o proprio perfil (senao 403).
 */
@Component
@ConditionalOnProperty(name = "ia.core.stub", havingValue = "true")
class StubCoreGateway implements CoreGateway {

    private static final Logger log = LoggerFactory.getLogger(StubCoreGateway.class);

    static final Map<Long, PerfilColaborador> COLABORADORES = Map.of(
            1L, new PerfilColaborador(1L, "Ana Souza", "Analista de QA", "QA", 3, 4, 8.1),
            2L, new PerfilColaborador(2L, "Carlos Lima", "Desenvolvedor Backend", "DESENVOLVIMENTO", 5, 6, 7.4),
            3L, new PerfilColaborador(3L, "Beatriz Rocha", "Engenheira DevOps", "DEVOPS", 8, 12, 9.0));

    static final List<CursoCatalogo> CURSOS = List.of(
            new CursoCatalogo(1L, "Docker na Prática", "DEVOPS", "INTERMEDIARIO", 20),
            new CursoCatalogo(2L, "Introdução a DevOps", "DEVOPS", "INICIANTE", 12),
            new CursoCatalogo(3L, "Kubernetes para Produção", "DEVOPS", "AVANCADO", 40),
            new CursoCatalogo(4L, "Fundamentos de Agile Testing", "QA", "INICIANTE", 16),
            new CursoCatalogo(5L, "Qualidade de Software", "QA", "INTERMEDIARIO", 24),
            new CursoCatalogo(6L, "Java com Spring Boot", "DESENVOLVIMENTO", "INTERMEDIARIO", 40),
            new CursoCatalogo(7L, "Arquitetura de Software e Microsserviços", "DESENVOLVIMENTO", "ESPECIALISTA", 36),
            new CursoCatalogo(8L, "SQL e Modelagem de Dados", "DADOS", "INICIANTE", 20),
            new CursoCatalogo(9L, "Machine Learning Aplicado", "DADOS", "AVANCADO", 36),
            new CursoCatalogo(10L, "Liderança de Equipes Técnicas", "GESTAO", "INTERMEDIARIO", 16));

    @PostConstruct
    void avisar() {
        log.warn("CoreGateway em modo STUB (ia.core.stub=true): dados fixos, sem chamar o Core. "
                + "Na versao final use CORE_STUB=false.");
    }

    @Override
    public PerfilColaborador buscarPerfil(Solicitante solicitante, long colaboradorId) {
        if (!solicitante.admin() && !Long.valueOf(colaboradorId).equals(solicitante.colaboradorId())) {
            throw new AcessoNegadoException("O Core negou o acesso: o usuário logado não pode ver esses dados.");
        }
        PerfilColaborador perfil = COLABORADORES.get(colaboradorId);
        if (perfil == null) {
            throw new NaoEncontradaException("Colaborador " + colaboradorId + " não encontrado.");
        }
        return perfil;
    }

    @Override
    public List<CursoCatalogo> listarCursos(Solicitante solicitante, String area) {
        return area == null ? CURSOS : CURSOS.stream().filter(c -> c.area().equals(area)).toList();
    }

    @Override
    public Optional<CursoCatalogo> buscarCurso(Solicitante solicitante, long cursoId) {
        return CURSOS.stream().filter(c -> c.id() == cursoId).findFirst();
    }
}
