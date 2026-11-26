package com.example.grupo_5_gameeducation.catalogo.api;

import com.example.grupo_5_gameeducation.comum.identidade.Solicitante;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST do catalogo (RF01). Depende so do contrato {@link CatalogoApi},
 * nunca do catalogo.internal.
 */
@RestController
@RequestMapping("/api/cursos")
@Tag(name = "Cursos", description = "RF01: catalogo de cursos")
public class CursoController {

    private final CatalogoApi catalogo;

    public CursoController(CatalogoApi catalogo) {
        this.catalogo = catalogo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um curso (so ADMIN)")
    public CursoResponse cadastrar(@Valid @RequestBody CriarCursoRequest request, Solicitante solicitante) {
        return CursoResponse.from(catalogo.cadastrar(request, solicitante));
    }

    @GetMapping
    @Operation(summary = "Lista os cursos, com filtro opcional por area")
    public List<CursoResponse> listar(
            @Parameter(description = "DEVOPS, QA, DESENVOLVIMENTO, DADOS ou GESTAO")
            @RequestParam(required = false) Area area) {
        return catalogo.listar(area).stream().map(CursoResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um curso pelo id")
    public CursoResponse buscarPorId(@PathVariable Long id) {
        return CursoResponse.from(catalogo.buscarPorId(id));
    }
}
