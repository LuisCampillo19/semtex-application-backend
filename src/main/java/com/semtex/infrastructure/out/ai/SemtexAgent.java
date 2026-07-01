package com.semtex.infrastructure.out.ai;

import dev.langchain4j.service.SystemMessage;

/**
 * Agente declarativo de LangChain4j. {@code AiServices} genera la implementación que gestiona
 * NATIVAMENTE el ciclo de function calling (el modelo pide una herramienta, se ejecuta y el
 * resultado vuelve al modelo) — resolviendo el bug del parsing manual que cortaba la respuesta.
 */
public interface SemtexAgent {

    @SystemMessage("""
            Eres Semtex, un copiloto financiero y administrativo para PyMEs.
            Respondes siempre en español, de forma clara y concisa.

            Dispones de herramientas:
            - consultarDatosFinancieros: para buscar filas por un campo y valor exactos.
            - compararPeriodos: para comparar el total de un monto entre dos periodos.
            - enviarCorreo: para enviar un correo cuando el usuario lo pida explícitamente.

            Usa las herramientas cuando aporten datos reales; no inventes cifras.
            Cuando hagas cálculos financieros, muestra el desglose. Si faltan datos, dilo con honestidad.
            """)
    String chat(String userMessage);
}
