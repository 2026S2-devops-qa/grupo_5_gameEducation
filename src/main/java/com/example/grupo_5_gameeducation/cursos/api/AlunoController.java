package com.example.grupo_5_gameeducation.cursos.api;

import com.example.grupo_5_gameeducation.cursos.internal.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alunos")
@Tag(name = "Alunos", description = "Gamificacao da US1: libera 3 cursos ao concluir um curso com media acima de 7,0")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um aluno")
    public AlunoResponse criar(@RequestBody CriarAlunoRequest request) {
        return alunoService.criar(request);
    }

    @GetMapping
    @Operation(summary = "Lista os alunos cadastrados")
    public List<AlunoResponse> listar() {
        return alunoService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um aluno pelo id")
    public AlunoResponse buscarPorId(@PathVariable Long id) {
        return alunoService.buscarPorId(id);
    }

    @PostMapping("/{id}/conclusoes")
    @Operation(summary = "Registra a conclusao de um curso e aplica a regra da US1")
    public AlunoResponse concluirCurso(@PathVariable Long id, @RequestBody ConcluirCursoRequest request) {
        return alunoService.concluirCurso(id, request);
    }
}
