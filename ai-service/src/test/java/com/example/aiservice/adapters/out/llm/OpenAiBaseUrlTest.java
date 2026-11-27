package com.example.aiservice.adapters.out.llm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Os tres exemplos do .env.example (secao 5.6) funcionam sem mudar codigo. */
class OpenAiBaseUrlTest {

    @Test
    void openAiSemCaminhoGanhaV1() {
        assertThat(OpenAiBaseUrl.normalizar("https://api.openai.com")).isEqualTo("https://api.openai.com/v1");
        assertThat(OpenAiBaseUrl.normalizar("https://api.openai.com/")).isEqualTo("https://api.openai.com/v1");
    }

    @Test
    void ollamaSemCaminhoGanhaV1() {
        assertThat(OpenAiBaseUrl.normalizar("http://host.docker.internal:11434"))
                .isEqualTo("http://host.docker.internal:11434/v1");
    }

    @Test
    void geminiComCaminhoFicaComoEsta() {
        assertThat(OpenAiBaseUrl.normalizar("https://generativelanguage.googleapis.com/v1beta/openai"))
                .isEqualTo("https://generativelanguage.googleapis.com/v1beta/openai");
    }

    @Test
    void urlQueJaTemV1NaoDuplica() {
        assertThat(OpenAiBaseUrl.normalizar("https://api.openai.com/v1")).isEqualTo("https://api.openai.com/v1");
    }

    @Test
    void vaziaUsaOPadraoDaOpenAi() {
        assertThat(OpenAiBaseUrl.normalizar(null)).isEqualTo(OpenAiBaseUrl.PADRAO);
        assertThat(OpenAiBaseUrl.normalizar("  ")).isEqualTo(OpenAiBaseUrl.PADRAO);
    }
}
