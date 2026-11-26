package com.example.grupo_5_gameeducation.catalogo.internal;

import com.example.grupo_5_gameeducation.catalogo.api.Area;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoCatalogoRepository extends JpaRepository<CursoCatalogoEntity, Long> {

    List<CursoCatalogoEntity> findAllByOrderByIdAsc();

    List<CursoCatalogoEntity> findByAreaOrderByIdAsc(Area area);

    boolean existsByTituloIgnoreCase(String titulo);

    Optional<CursoCatalogoEntity> findFirstByTituloIgnoreCase(String titulo);
}
