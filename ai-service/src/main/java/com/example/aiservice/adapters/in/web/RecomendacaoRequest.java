package com.example.aiservice.adapters.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo de POST /api/ia/recomendacoes (secao 5.4): {"objetivo":"quero crescer em DevOps"}. */
public record RecomendacaoRequest(
        @NotBlank(message = "Informe o objetivo.")
        @Size(max = 500, message = "O objetivo pode ter no máximo 500 caracteres.")
        String objetivo) {
}
