package com.example.grupo_5_gameeducation.cursos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.grupo_5_gameeducation.comum.erro.NaoEncontradoException;
import com.example.grupo_5_gameeducation.comum.erro.RequisicaoInvalidaException;
import com.example.grupo_5_gameeducation.cursos.api.AlunoResponse;
import com.example.grupo_5_gameeducation.cursos.api.ConcluirCursoRequest;
import com.example.grupo_5_gameeducation.cursos.api.CriarAlunoRequest;
import com.example.grupo_5_gameeducation.cursos.api.CursosApi;
import com.example.grupo_5_gameeducation.domain.Plano;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * US1 integrada ao catalogo: o modulo cursos valida o curso pelo contrato publico CatalogoApi
 * antes de aplicar a regra do dominio.
 */
@SpringBootTest
@Transactional
class AlunoServiceTest {

    @Autowired
    private CursosApi cursos;

    @Test
    @DisplayName("Concluir curso do catalogo com media 8,5 libera 3 cursos (regra da US1)")
    void cursoDoCatalogoAplicaUS1() {
        AlunoResponse aluno = cursos.criar(new CriarAlunoRequest("Bruno", Plano.BASICO));

        AlunoResponse depois = cursos.concluirCurso(aluno.id(),
                new ConcluirCursoRequest("Fundamentos de Agile Testing", 8.5));

        assertThat(depois.cursosLiberados()).isEqualTo(3);
        assertThat(depois.cursosConquistados()).isEqualTo(1);
    }

    @Test
    @DisplayName("Concluir curso que nao existe no catalogo e rejeitado (400)")
    void cursoForaDoCatalogoRejeitado() {
        AlunoResponse aluno = cursos.criar(new CriarAlunoRequest("Bruno", Plano.BASICO));
        ConcluirCursoRequest inexistente = new ConcluirCursoRequest("Curso Que Nao Existe", 9.0);

        assertThatThrownBy(() -> cursos.concluirCurso(aluno.id(), inexistente))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessageContaining("nao existe no catalogo");
        assertThat(cursos.buscarPorId(aluno.id()).cursosLiberados()).isZero();
    }

    @Test
    @DisplayName("Mesmo curso escrito com outra caixa nao bonifica de novo (usa o titulo oficial)")
    void mesmoCursoComOutraCaixaNaoBonificaDeNovo() {
        AlunoResponse aluno = cursos.criar(new CriarAlunoRequest("Bruno", Plano.BASICO));

        cursos.concluirCurso(aluno.id(), new ConcluirCursoRequest("Docker na Prática", 9.0));
        AlunoResponse depois = cursos.concluirCurso(aluno.id(), new ConcluirCursoRequest("DOCKER NA PRÁTICA", 9.0));

        assertThat(depois.cursosLiberados()).isEqualTo(3);
        assertThat(depois.cursosConquistados()).isEqualTo(1);
    }

    @Test
    @DisplayName("Aluno inexistente da 404")
    void alunoInexistente() {
        assertThatThrownBy(() -> cursos.buscarPorId(999_999L)).isInstanceOf(NaoEncontradoException.class);
    }
}
