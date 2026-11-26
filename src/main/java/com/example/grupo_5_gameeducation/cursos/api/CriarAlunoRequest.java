package com.example.grupo_5_gameeducation.cursos.api;

import com.example.grupo_5_gameeducation.domain.Plano;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarAlunoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotNull Plano plano) {
}
