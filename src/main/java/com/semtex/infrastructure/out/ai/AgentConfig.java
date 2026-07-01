package com.semtex.infrastructure.out.ai;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Construye el {@link SemtexAgent} con {@code AiServices}, enlazando el modelo Ollama y las
 * herramientas. AiServices implementa el bucle de tool-calling automáticamente.
 */
@Configuration
public class AgentConfig {

    @Bean
    public SemtexAgent semtexAgent(OllamaChatModel model, SemtexAgentTools tools) {
        return AiServices.builder(SemtexAgent.class)
                .chatLanguageModel(model)
                .tools(tools)
                .build();
    }
}
