package com.example.aiservice.adapters.out.persistence;

import com.example.aiservice.application.port.RepositorioAcoes;
import com.example.aiservice.domain.AcaoAgente;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador da porta {@link RepositorioAcoes} com Spring Data JPA (H2). */
@Component
class JpaRepositorioAcoes implements RepositorioAcoes {

    private final AcaoAgenteJpaRepository jpa;

    JpaRepositorioAcoes(AcaoAgenteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public AcaoAgente salvar(AcaoAgente acao) {
        AcaoAgenteEntity salva = jpa.save(AcaoAgenteEntity.de(acao));
        acao.definirId(salva.paraDominio().getId());
        return acao;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AcaoAgente> buscar(long id) {
        return jpa.findById(id).map(AcaoAgenteEntity::paraDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcaoAgente> listarDoUsuario(String login) {
        return jpa.findBySolicitadoPorOrderByIdDesc(login).stream().map(AcaoAgenteEntity::paraDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcaoAgente> listarTodas() {
        return jpa.findAllByOrderByIdDesc().stream().map(AcaoAgenteEntity::paraDominio).toList();
    }
}
