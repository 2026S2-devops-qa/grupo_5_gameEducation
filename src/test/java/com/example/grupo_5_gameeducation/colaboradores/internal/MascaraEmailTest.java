package com.example.grupo_5_gameeducation.colaboradores.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MascaraEmailTest {

    @Test
    @DisplayName("Mantem so a primeira letra e o dominio")
    void mascaraEmail() {
        assertThat(MascaraEmail.mascarar("ana@empresa.com")).isEqualTo("a***@empresa.com");
        assertThat(MascaraEmail.mascarar("carlos.lima@empresa.com")).isEqualTo("c***@empresa.com");
    }

    @Test
    @DisplayName("Email de uma letra antes do @ tambem e mascarado")
    void emailCurto() {
        assertThat(MascaraEmail.mascarar("a@empresa.com")).isEqualTo("a***@empresa.com");
    }

    @Test
    @DisplayName("Valor sem @ nao vaza nada")
    void semArroba() {
        assertThat(MascaraEmail.mascarar("sem-arroba")).isEqualTo("***");
        assertThat(MascaraEmail.mascarar("@empresa.com")).isEqualTo("***");
    }

    @Test
    @DisplayName("Nulo ou vazio fica como esta")
    void nuloOuVazio() {
        assertThat(MascaraEmail.mascarar(null)).isNull();
        assertThat(MascaraEmail.mascarar("")).isEmpty();
    }
}
