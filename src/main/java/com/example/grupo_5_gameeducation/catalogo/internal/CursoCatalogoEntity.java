package com.example.grupo_5_gameeducation.catalogo.internal;

import com.example.grupo_5_gameeducation.catalogo.api.Area;
import com.example.grupo_5_gameeducation.catalogo.api.CursoResumo;
import com.example.grupo_5_gameeducation.catalogo.api.Nivel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Adaptador de saida (JPA). Fica no internal: nenhum outro modulo enxerga esta classe. */
@Entity
@Table(name = "curso_catalogo")
public class CursoCatalogoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Area area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Nivel nivel;

    @Column(nullable = false)
    private int cargaHoraria;

    protected CursoCatalogoEntity() {
    }

    public CursoCatalogoEntity(String titulo, Area area, Nivel nivel, int cargaHoraria) {
        this.titulo = titulo;
        this.area = area;
        this.nivel = nivel;
        this.cargaHoraria = cargaHoraria;
    }

    CursoResumo paraResumo() {
        return new CursoResumo(id, titulo, area, nivel, cargaHoraria);
    }
}
