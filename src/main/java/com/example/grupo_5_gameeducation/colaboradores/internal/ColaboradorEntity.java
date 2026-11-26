package com.example.grupo_5_gameeducation.colaboradores.internal;

import com.example.grupo_5_gameeducation.catalogo.api.Area;
import com.example.grupo_5_gameeducation.colaboradores.api.PerfilColaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Adaptador de saida (JPA). So os dados que o negocio usa: sem CPF, telefone ou endereco
 * (minimizacao de dados pessoais, LGPD).
 */
@Entity
@Table(name = "colaborador")
public class ColaboradorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false, length = 80)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Area area;

    @Column(nullable = false)
    private int anosExperiencia;

    @Column(nullable = false)
    private int cursosConcluidos;

    @Column(nullable = false)
    private double mediaNotas;

    protected ColaboradorEntity() {
    }

    public ColaboradorEntity(String nome, String email, String cargo, Area area,
            int anosExperiencia, int cursosConcluidos, double mediaNotas) {
        this.nome = nome;
        this.email = email;
        this.cargo = cargo;
        this.area = area;
        this.anosExperiencia = anosExperiencia;
        this.cursosConcluidos = cursosConcluidos;
        this.mediaNotas = mediaNotas;
    }

    Long getId() {
        return id;
    }

    String getNome() {
        return nome;
    }

    String getEmail() {
        return email;
    }

    String getCargo() {
        return cargo;
    }

    Area getArea() {
        return area;
    }

    int getAnosExperiencia() {
        return anosExperiencia;
    }

    int getCursosConcluidos() {
        return cursosConcluidos;
    }

    double getMediaNotas() {
        return mediaNotas;
    }

    PerfilColaborador paraPerfil() {
        return new PerfilColaborador(id, nome, area, anosExperiencia, cursosConcluidos, mediaNotas);
    }
}
