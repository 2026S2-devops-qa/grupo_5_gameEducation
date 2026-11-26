package com.example.grupo_5_gameeducation.colaboradores.api;

import com.example.grupo_5_gameeducation.catalogo.api.Area;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cadastro de colaborador. So o necessario para o negocio: nada de CPF, telefone ou
 * endereco (minimizacao de dados pessoais, LGPD).
 */
public record CriarColaboradorRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 80) String cargo,
        @NotNull Area area,
        @NotNull @Min(0) @Max(60) Integer anosExperiencia,
        @NotNull @Min(0) Integer cursosConcluidos,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") Double mediaNotas) {
}
