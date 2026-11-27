package com.example.aiservice.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Regra do dominio contra alucinacao: curso so entra se estiver nos trechos recuperados. */
class RecomendacaoTest {

    private final Trecho trilhas = new Trecho("politica-de-trilhas.md",
            "Trilha de DevOps: 1. Introdução a DevOps 2. Docker na Prática 3. Kubernetes para Produção", 0.8);
    private final Trecho catalogo = new Trecho("catalogo-cursos.md",
            "Docker na Prática: área DEVOPS, nível INTERMEDIARIO, 20 horas.", 0.7);

    @Test
    void cursoQueNaoApareceNosTrechosEDescartado() {
        RespostaGerada gerada = new RespostaGerada("Faça estes cursos.", List.of(
                new CursoRecomendado("Docker na Prática", "base de containers"),
                new CursoRecomendado("Curso Inventado de Blockchain", "alucinação")));

        Recomendacao recomendacao = Recomendacao.fundamentada(gerada, List.of(trilhas));

        assertThat(recomendacao.cursos()).extracting(CursoRecomendado::titulo).containsExactly("Docker na Prática");
    }

    @Test
    void comparaTituloSemDiferenciarMaiusculasECursoRepetidoApareceUmaVez() {
        RespostaGerada gerada = new RespostaGerada("ok", List.of(
                new CursoRecomendado("docker na prática", "a"),
                new CursoRecomendado("Docker na Prática ", "b")));

        Recomendacao recomendacao = Recomendacao.fundamentada(gerada, List.of(trilhas));

        assertThat(recomendacao.cursos()).hasSize(1);
    }

    @Test
    void fontesSaoOsArquivosDosTrechosSemRepetir() {
        Trecho outroPedacoDasTrilhas = new Trecho("politica-de-trilhas.md", "outro pedaço", 0.5);

        Recomendacao recomendacao = Recomendacao.fundamentada(
                new RespostaGerada("ok", List.of()), List.of(trilhas, catalogo, outroPedacoDasTrilhas));

        assertThat(recomendacao.fontes()).containsExactly("politica-de-trilhas.md", "catalogo-cursos.md");
    }

    @Test
    void semTrechosRespondeQueNaoEncontrouSemCursoESemFonte() {
        Recomendacao recomendacao = Recomendacao.fundamentada(
                new RespostaGerada("invente algo", List.of(new CursoRecomendado("Docker na Prática", "x"))), List.of());

        assertThat(recomendacao.resposta()).isEqualTo(Recomendacao.SEM_BASE);
        assertThat(recomendacao.cursos()).isEmpty();
        assertThat(recomendacao.fontes()).isEmpty();
    }

    @Test
    void respostaVaziaDoLlmViraMensagemDeSemBase() {
        Recomendacao recomendacao = Recomendacao.fundamentada(new RespostaGerada(" ", null), List.of(trilhas));

        assertThat(recomendacao.resposta()).isEqualTo(Recomendacao.SEM_BASE);
        assertThat(recomendacao.cursos()).isEmpty();
    }
}
