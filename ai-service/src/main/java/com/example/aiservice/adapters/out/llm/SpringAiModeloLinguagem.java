package com.example.aiservice.adapters.out.llm;

import com.example.aiservice.application.IaIndisponivelException;
import com.example.aiservice.application.port.ModeloLinguagem;
import com.example.aiservice.domain.RespostaGerada;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adaptador de saida da porta {@link ModeloLinguagem} com o ChatClient do Spring AI.
 * O provedor (OpenAI, Gemini, Ollama) e o modelo vem do .env, sem mudar o nucleo.
 */
@Component
class SpringAiModeloLinguagem implements ModeloLinguagem {

    private static final Logger log = LoggerFactory.getLogger(SpringAiModeloLinguagem.class);

    private final ChatClient chatClient;
    private final boolean configurado;

    SpringAiModeloLinguagem(ChatClient.Builder builder, @Value("${spring.ai.openai.api-key:}") String apiKey) {
        this.chatClient = builder.build();
        this.configurado = apiKey != null && !apiKey.isBlank();
    }

    @Override
    public boolean configurado() {
        return configurado;
    }

    @Override
    public RespostaGerada gerar(String instrucoesSistema, String mensagemUsuario) {
        try {
            // .entity(...) pede a resposta em JSON no formato do record (saida estruturada)
            RespostaGerada resposta = chatClient.prompt()
                    .system(instrucoesSistema)
                    .user(mensagemUsuario)
                    .call()
                    .entity(RespostaGerada.class);
            return resposta != null ? resposta : new RespostaGerada(null, List.of());
        } catch (RuntimeException e) {
            log.error("Falha ao chamar o LLM: {}", e.getMessage());
            throw new IaIndisponivelException(IaIndisponivelException.FALHA_LLM, e);
        }
    }

    @Override
    public String orientar(String instrucoesSistema, String mensagemUsuario, Object ferramentas) {
        try {
            // .tools(...): o Spring AI le os metodos @Tool e executa as chamadas que o modelo pedir
            String texto = chatClient.prompt()
                    .system(instrucoesSistema)
                    .user(mensagemUsuario)
                    .tools(ferramentas)
                    .call()
                    .content();
            return texto == null || texto.isBlank() ? "Plano montado. Veja as ações propostas abaixo." : texto;
        } catch (RuntimeException e) {
            log.error("Falha ao chamar o LLM (agente): {}", e.getMessage());
            throw new IaIndisponivelException(IaIndisponivelException.FALHA_LLM, e);
        }
    }
}
