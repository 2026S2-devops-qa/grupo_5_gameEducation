package com.example.aiservice.adapters.out.llm;

import java.net.URI;

/**
 * Normaliza o OPENAI_BASE_URL do .env (secao 5.6 do plano).
 *
 * O Spring AI 2.0 usa o SDK oficial da OpenAI, que espera o "/v1" dentro da URL
 * (https://api.openai.com/v1). O .env.example do grupo usa a forma sem caminho
 * (https://api.openai.com), entao acrescentamos "/v1" quando a URL nao tem caminho:
 *
 *   https://api.openai.com                                  -> https://api.openai.com/v1
 *   http://host.docker.internal:11434 (Ollama)              -> http://host.docker.internal:11434/v1
 *   https://generativelanguage.googleapis.com/v1beta/openai -> sem mudanca (Gemini)
 */
public final class OpenAiBaseUrl {

    public static final String PADRAO = "https://api.openai.com/v1";

    private OpenAiBaseUrl() {
    }

    public static String normalizar(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return PADRAO;
        }
        String url = baseUrl.strip();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String caminho;
        try {
            caminho = URI.create(url).getPath();
        } catch (IllegalArgumentException e) {
            return url; // URL estranha: deixa o SDK reclamar com a mensagem dele
        }
        return (caminho == null || caminho.isEmpty()) ? url + "/v1" : url;
    }
}
