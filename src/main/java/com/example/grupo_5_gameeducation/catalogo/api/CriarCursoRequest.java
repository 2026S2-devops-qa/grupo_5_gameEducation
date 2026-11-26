package com.example.grupo_5_gameeducation.catalogo.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarCursoRequest(
        @NotBlank @Size(max = 120) String titulo,
        @NotNull Area area,
        @NotNull Nivel nivel,
        @NotNull @Min(1) @Max(500) Integer cargaHoraria) {
}
