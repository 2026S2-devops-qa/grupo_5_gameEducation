package com.example.aiservice.domain;

/** Ciclo de vida de uma acao proposta pelo agente (RF04). So o usuario tira a acao de AGUARDANDO. */
public enum StatusAcao {
    AGUARDANDO_AUTORIZACAO,
    EXECUTADA,
    CANCELADA
}
