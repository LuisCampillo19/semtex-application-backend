package com.semtex.domain.port.out;

/**
 * Puerto de salida: pasarela hacia el modelo de IA (frontera tecnológica: LangChain4j + Ollama).
 *
 * El dominio solo conoce esta interfaz; la implementación gestiona el modelo local, los timeouts
 * y —desde el contexto del agente— el ciclo de function calling.
 */
public interface AiGatewayPort {

    AiAnswer ask(AiRequest request);

    record AiRequest(String userMessage, String contextText) {}

    record AiAnswer(String text, Integer tokensUsed) {}
}
