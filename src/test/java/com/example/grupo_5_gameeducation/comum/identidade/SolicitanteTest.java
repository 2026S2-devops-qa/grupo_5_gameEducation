package com.example.grupo_5_gameeducation.comum.identidade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SolicitanteTest {

    @Test
    @DisplayName("Le os headers do gateway: login, papeis e colaboradorId")
    void leHeadersDoGateway() {
        Solicitante ana = Solicitante.dosHeaders("ana", "COLABORADOR", "1");

        assertThat(ana.login()).isEqualTo("ana");
        assertThat(ana.roles()).containsExactly("COLABORADOR");
        assertThat(ana.colaboradorId()).isEqualTo(1L);
        assertThat(ana.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("Admin vem com X-Colaborador-Id vazio, que vira null")
    void adminSemColaborador() {
        Solicitante admin = Solicitante.dosHeaders("admin", "ADMIN", "");

        assertThat(admin.isAdmin()).isTrue();
        assertThat(admin.colaboradorId()).isNull();
    }

    @Test
    @DisplayName("Varios papeis separados por virgula, com espacos")
    void variosPapeis() {
        Solicitante s = Solicitante.dosHeaders("x", " COLABORADOR , ADMIN ", null);

        assertThat(s.roles()).isEqualTo(Set.of("COLABORADOR", "ADMIN"));
        assertThat(s.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("Sem headers (chamada sem gateway): nenhum papel, nenhum acesso")
    void semHeaders() {
        Solicitante anonimo = Solicitante.dosHeaders(null, null, null);

        assertThat(anonimo.isAdmin()).isFalse();
        assertThat(anonimo.podeAcessarColaborador(1L)).isFalse();
    }

    @Test
    @DisplayName("X-Colaborador-Id que nao e numero vira null")
    void colaboradorIdInvalido() {
        assertThat(Solicitante.dosHeaders("ana", "COLABORADOR", "abc").colaboradorId()).isNull();
    }

    @Test
    @DisplayName("Acesso a colaborador: ADMIN ou o proprio")
    void regraDeAcesso() {
        Solicitante ana = Solicitante.dosHeaders("ana", "COLABORADOR", "1");
        Solicitante admin = Solicitante.dosHeaders("admin", "ADMIN", "");

        assertThat(ana.podeAcessarColaborador(1L)).isTrue();
        assertThat(ana.podeAcessarColaborador(2L)).isFalse();
        assertThat(admin.podeAcessarColaborador(2L)).isTrue();
    }
}
