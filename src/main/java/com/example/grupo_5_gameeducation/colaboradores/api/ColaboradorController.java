package com.example.grupo_5_gameeducation.colaboradores.api;

import com.example.grupo_5_gameeducation.comum.identidade.Solicitante;
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
 * Adaptador de entrada REST dos colaboradores (RF01). Depende so do contrato
 * {@link ColaboradoresApi}. A identidade ({@link Solicitante}) vem dos headers do gateway.
 */
@RestController
@RequestMapping("/api/colaboradores")
@Tag(name = "Colaboradores", description = "RF01: cadastro e consulta de colaboradores")
public class ColaboradorController {

    private final ColaboradoresApi colaboradores;

    public ColaboradorController(ColaboradoresApi colaboradores) {
        this.colaboradores = colaboradores;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um colaborador (so ADMIN)")
    public ColaboradorResponse cadastrar(@Valid @RequestBody CriarColaboradorRequest request, Solicitante solicitante) {
        return colaboradores.cadastrar(request, solicitante);
    }

    @GetMapping
    @Operation(summary = "Lista todos os colaboradores (so ADMIN)")
    public List<ColaboradorResponse> listar(Solicitante solicitante) {
        return colaboradores.listar(solicitante);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um colaborador (ADMIN ou o proprio colaborador; email mascarado para nao ADMIN)")
    public ColaboradorResponse buscar(@PathVariable Long id, Solicitante solicitante) {
        return colaboradores.buscar(id, solicitante);
    }
}
