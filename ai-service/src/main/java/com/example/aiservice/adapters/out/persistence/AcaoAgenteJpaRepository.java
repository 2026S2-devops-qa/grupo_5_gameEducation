package com.example.aiservice.adapters.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface AcaoAgenteJpaRepository extends JpaRepository<AcaoAgenteEntity, Long> {

    List<AcaoAgenteEntity> findBySolicitadoPorOrderByIdDesc(String solicitadoPor);

    List<AcaoAgenteEntity> findAllByOrderByIdDesc();
}
