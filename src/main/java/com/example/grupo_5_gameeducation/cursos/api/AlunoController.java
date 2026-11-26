package com.example.grupo_5_gameeducation.cursos.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST da US1. Depende so do contrato {@link CursosApi}: nao importa
 * nada do cursos.internal (o ArchUnit garante).
 */
@RestController
@RequestMapping("/api/alunos")
@Tag(name = "Alunos", description = "Gamificacao da US1: libera 3 cursos ao concluir um curso com media acima de 7,0")
public class AlunoController {

    private final CursosApi cursos;

    public AlunoController(CursosApi cursos) {
        this.cursos = cursos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um aluno")
    public AlunoResponse criar(@Valid @RequestBody CriarAlunoRequest request) {
        return cursos.criar(request);
    }

    @GetMapping
    @Operation(summary = "Lista os alunos cadastrados")
    public List<AlunoResponse> listar() {
        return cursos.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um aluno pelo id")
    public AlunoResponse buscarPorId(@PathVariable Long id) {
        return cursos.buscarPorId(id);
    }

    @PostMapping("/{id}/conclusoes")
    @Operation(summary = "Registra a conclusao de um curso do catalogo e aplica a regra da US1")
    public AlunoResponse concluirCurso(@PathVariable Long id, @Valid @RequestBody ConcluirCursoRequest request) {
        return cursos.concluirCurso(id, request);
    }
}
