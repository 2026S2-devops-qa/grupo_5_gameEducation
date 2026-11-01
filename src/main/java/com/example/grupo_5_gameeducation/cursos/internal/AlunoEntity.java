package com.example.grupo_5_gameeducation.cursos.internal;

import com.example.grupo_5_gameeducation.domain.Aluno;
import com.example.grupo_5_gameeducation.domain.Plano;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "aluno")
public class AlunoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plano plano;

    @ElementCollection
    @CollectionTable(name = "aluno_curso_concluido", joinColumns = @JoinColumn(name = "aluno_id"))
    @Column(name = "titulo_curso")
    private Set<String> cursosConcluidos = new HashSet<>();

    private int cursosLiberados;

    protected AlunoEntity() {
    }

    public AlunoEntity(String nome, Plano plano) {
        this.nome = nome;
        this.plano = plano;
    }
    
    public Aluno paraDominio() {
        return new Aluno(nome, plano, cursosConcluidos, cursosLiberados);
    }

    public void atualizarEstado(Aluno aluno) {
        this.cursosConcluidos = new HashSet<>(aluno.getCursosConcluidos());
        this.cursosLiberados = aluno.getCursosLiberados();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Plano getPlano() {
        return plano;
    }

    public int getCursosLiberados() {
        return cursosLiberados;
    }

    public int getCursosConquistados() {
        return cursosConcluidos.size();
    }
}
