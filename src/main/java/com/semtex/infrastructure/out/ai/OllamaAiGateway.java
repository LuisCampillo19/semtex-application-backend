package com.semtex.infrastructure.out.ai;

import com.semtex.domain.port.out.AiGatewayPort;
import org.springframework.stereotype.Component;

/**
 * Adaptador del {@link AiGatewayPort} sobre el agente LangChain4j (Ollama + Llama 3.1 8B).
 *
 * Delega en {@link SemtexAgent}, que gestiona el ciclo de function calling. El contexto financiero
 * pre-cargado se antepone al mensaje del usuario; el agente puede además invocar herramientas para
 * obtener más datos, comparar periodos o enviar correos.
 */
@Component
public class OllamaAiGateway implements AiGatewayPort {

    private final SemtexAgent agent;

    public OllamaAiGateway(SemtexAgent agent) {
        this.agent = agent;
    }

    @Override
    public AiAnswer ask(AiRequest request) {
        String userMessage = (request.contextText() == null || request.contextText().isBlank())
                ? request.userMessage()
                : "DATOS FINANCIEROS DISPONIBLES:\n" + request.contextText()
                  + "\n\nPregunta del usuario: " + request.userMessage();
        String text = agent.chat(userMessage);
        return new AiAnswer(text, null);
    }
}
