package com.example.aiservice.adapters.out.llm;

import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Le o OPENAI_BASE_URL do .env e entrega ao Spring AI ja normalizado (ver {@link OpenAiBaseUrl}).
 * Assim o mesmo .env serve para OpenAI, Gemini e Ollama sem mexer em codigo.
 * Registrado em META-INF/spring.factories.
 */
public class OpenAiBaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String PROPRIEDADE = "spring.ai.openai.base-url";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String informada = environment.getProperty("OPENAI_BASE_URL");
        String normalizada = OpenAiBaseUrl.normalizar(informada);
        environment.getPropertySources()
                .addFirst(new MapPropertySource("openaiBaseUrlNormalizada", Map.of(PROPRIEDADE, normalizada)));
    }
}
