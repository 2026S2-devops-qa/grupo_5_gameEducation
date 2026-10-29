package com.example.grupo_5_gameeducation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes de aceitacao da US1 - liberacao automatica de 3 novos cursos ao concluir um
 * curso com media acima de 7,0.
 *
 * Um teste por cenario BDD, um cenario por integrante. Cada teste foi escrito na etapa
 * RED, antes da implementacao, e nao foi alterado depois. Quem evolui ao longo do ciclo
 * TDD e o Aluno.java, onde as etapas RED, GREEN e BLUE de cada cenario estao registradas.
 * O que muda em cada teste e so o resultado da execucao, anotado no javadoc do metodo.
 *
 * Um quarto teste, fora dos 3 cenarios de BDD, cobre o construtor de reconstrucao e o
 * getter de cursosConcluidos. Essa capacidade existe para que a camada de persistencia
 * (AlunoEntity/AlunoService) reaproveite esta mesma regra, em vez de reimplementa-la.
 */
class AlunoTest {

    /**
     * Cenario 1 da US1 - Bruno da Silveira Escanhoela.
     *
     * BDD:
     *   Dado que o aluno possui assinatura basica ativa
     *   E concluiu o curso "Fundamentos de Agile Testing"
     *   Quando a media final registrada for 8,5
     *   E a conclusao for validada pela plataforma
     *   Entao o sistema deve liberar 3 novos cursos para o aluno
     *   E o total de cursos conquistados deve ser 1
     *
     * Execucao ao longo do ciclo (o Aluno.java e que muda, nao este teste):
     *   RED   -> falha: UnsupportedOperationException, regra ainda nao implementada.
     *   GREEN -> passa: getCursosLiberados() = 3, getCursosConquistados() = 1.
     *   BLUE  -> passa: extrair as constantes de negocio nao mudou o comportamento.
     */
    @Test
    @DisplayName("Cenario 1 (Bruno): media acima de 7,0 libera 3 novos cursos")
    void deveLiberarTresCursosQuandoMediaAcimaDeSete() {
        // ARRANGE
        Aluno aluno = new Aluno("Bruno da Silveira Escanhoela", Plano.BASICO);
        Curso curso = new Curso("Fundamentos de Agile Testing");

        // ACTION
        aluno.concluir(curso, 8.5);

        // ASSERT
        assertEquals(3, aluno.getCursosLiberados());
        assertEquals(1, aluno.getCursosConquistados());
    }

    /**
     * Cenario 2 da US1 - Gabriel Ferreira do Nascimento.
     *
     * BDD:
     *   Dado que o aluno possui assinatura basica ativa
     *   E concluiu o curso "Introducao a DevOps"
     *   Quando a media final registrada for menor ou igual a 7,0 (6,5 e 7,0)
     *   E a conclusao for validada pela plataforma
     *   Entao nenhum curso adicional deve ser liberado
     *   E o aluno permanece com 0 cursos liberados
     *
     * Testa as duas medias do BDD (6,5 e 7,0) num unico teste, para manter a evidencia
     * do ciclo TDD em exatamente 3 execucoes, uma por cenario.
     *
     * Execucao ao longo do ciclo (o Aluno.java e que muda, nao este teste):
     *   RED   -> falha: mesma UnsupportedOperationException do stub.
     *   GREEN -> passa: a regra do Cenario 1 usa "media > 7,0", entao 6,5 e 7,0 nao
     *            liberam bonus.
     *   BLUE  -> passa: extrair o metodo aprovado() deu nome a fronteira "maior que".
     */
    @Test
    @DisplayName("Cenario 2 (Gabriel): media menor ou igual a 7,0 nao libera cursos")
    void naoDeveLiberarCursosQuandoMediaNaoSuperaSete() {
        // ARRANGE
        Aluno alunoMedia65 = new Aluno("Gabriel Ferreira do Nascimento", Plano.BASICO);
        Aluno alunoMedia70 = new Aluno("Gabriel Ferreira do Nascimento", Plano.BASICO);
        Curso curso = new Curso("Introducao a DevOps");

        // ACTION
        alunoMedia65.concluir(curso, 6.5);
        alunoMedia70.concluir(curso, 7.0);

        // ASSERT
        assertEquals(0, alunoMedia65.getCursosLiberados());
        assertEquals(0, alunoMedia70.getCursosLiberados());
    }

    /**
     * Cenario 3 da US1 - Joao Guilherme Volta Kinol.
     *
     * BDD:
     *   Dado que o aluno concluiu o curso "Qualidade de Software" com media 9,0
     *   E ja recebeu os 3 cursos referentes a essa conclusao
     *   Quando a conclusao do mesmo curso for registrada novamente
     *   Entao a liberacao nao deve ocorrer uma segunda vez
     *   E o aluno continua com 3 cursos liberados e 1 curso conquistado
     *
     * Execucao ao longo do ciclo (o Aluno.java e que muda, nao este teste):
     *   RED   -> falha: sem a guarda, a 2a conclusao bonifica de novo (liberados = 6,
     *            esperado 3).
     *   GREEN -> passa: Set.add() devolve false na 2a conclusao, usado como guarda.
     *   BLUE  -> passa: guard clause nomeada nao mudou o comportamento.
     */
    @Test
    @DisplayName("Cenario 3 (Joao): conclusao repetida do mesmo curso nao bonifica de novo")
    void naoDeveLiberarCursosDuasVezesParaOMesmoCurso() {
        // ARRANGE
        Aluno aluno = new Aluno("Joao Guilherme Volta Kinol", Plano.BASICO);
        Curso curso = new Curso("Qualidade de Software");

        // ACTION
        aluno.concluir(curso, 9.0);
        aluno.concluir(curso, 9.0); // deve ser ignorado

        // ASSERT
        assertEquals(3, aluno.getCursosLiberados());
        assertEquals(1, aluno.getCursosConquistados());
    }

    @Test
    @DisplayName("Reconstroi aluno com estado existente e preserva a guarda contra repeticao")
    void deveReconstruirAlunoComEstadoExistenteEPreservarGuardaDeRepeticao() {
        Aluno aluno = new Aluno(
                "Bruno da Silveira Escanhoela",
                Plano.BASICO,
                Set.of("Fundamentos de Agile Testing"),
                3);
        Curso mesmoCurso = new Curso("Fundamentos de Agile Testing");

        // ACTION
        aluno.concluir(mesmoCurso, 9.0);

        // ASSERT
        assertEquals(3, aluno.getCursosLiberados());
        assertEquals(1, aluno.getCursosConquistados());
        assertEquals(Set.of("Fundamentos de Agile Testing"), aluno.getCursosConcluidos());
    }
}
