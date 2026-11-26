package com.example.grupo_5_gameeducation.cursos.api;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConcluirCursoRequest(
        @NotBlank String tituloCurso,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") Double media) {
}
