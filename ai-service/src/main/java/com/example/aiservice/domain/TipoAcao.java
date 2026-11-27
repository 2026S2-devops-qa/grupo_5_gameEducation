package com.example.aiservice.domain;

/** Acoes que o agente de orientacao pode propor (secao 8.2 do plano). */
public enum TipoAcao {
    /** Inscrever o colaborador num curso do catalogo (vira registro no plano de estudos). */
    INSCRICAO,
    /** Agendar um acompanhamento daqui a N dias (vira lembrete com data). */
    ACOMPANHAMENTO
}
