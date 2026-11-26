package com.example.grupo_5_gameeducation.colaboradores.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColaboradorRepository extends JpaRepository<ColaboradorEntity, Long> {

    List<ColaboradorEntity> findAllByOrderByIdAsc();

    boolean existsByEmailIgnoreCase(String email);
}
