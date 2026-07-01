package com.semtex.infrastructure.out.ai;

import com.semtex.domain.port.in.SendEmailUseCase;
import com.semtex.domain.model.FinancialRecord;
import com.semtex.domain.port.in.QueryFinancialRecordsUseCase;
import com.semtex.domain.port.in.QueryFinancialRecordsUseCase.QueryCommand;
import com.semtex.infrastructure.tenant.TenantContext;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Herramientas del agente (function calling). Se ejecutan en el hilo de la request, por lo que
 * {@link TenantContext} está disponible y las consultas quedan acotadas al tenant por el filtro
 * Hibernate. Cada método es una capacidad real expuesta al modelo.
 */
@Component
public class SemtexAgentTools {

    private final QueryFinancialRecordsUseCase financialQuery;
    private final SendEmailUseCase sendEmail;

    public SemtexAgentTools(QueryFinancialRecordsUseCase financialQuery, SendEmailUseCase sendEmail) {
        this.financialQuery = financialQuery;
        this.sendEmail = sendEmail;
    }

    @Tool("Consulta registros financieros de la empresa filtrando por un campo y un valor exactos.")
    public String consultarDatosFinancieros(
            @P("nombre de la columna, p.ej. 'categoria'") String campo,
            @P("valor exacto a buscar, p.ej. 'Ventas'") String valor) {
        UUID org = TenantContext.requireOrganizationId();
        List<FinancialRecord> records = financialQuery.query(new QueryCommand(org, null, campo, valor, 50));
        if (records.isEmpty()) {
            return "No se encontraron registros con " + campo + " = " + valor + ".";
        }
        StringBuilder sb = new StringBuilder("Se encontraron " + records.size() + " registros:\n");
        for (FinancialRecord r : records) {
            sb.append("- ").append(r.getRowData()).append('\n');
        }
        return sb.toString();
    }

    @Tool("Compara el total de un monto entre dos periodos identificados por un campo (p.ej. 'mes').")
    public String compararPeriodos(
            @P("campo que identifica el periodo, p.ej. 'mes'") String campoPeriodo,
            @P("primer periodo, p.ej. 'enero'") String periodoA,
            @P("segundo periodo, p.ej. 'febrero'") String periodoB,
            @P("campo numérico a sumar, p.ej. 'ingreso'") String campoMonto) {
        UUID org = TenantContext.requireOrganizationId();
        double totalA = sum(org, campoPeriodo, periodoA, campoMonto);
        double totalB = sum(org, campoPeriodo, periodoB, campoMonto);
        double diff = totalB - totalA;
        return String.format(
                "Total %s (%s=%s): %.2f. Total %s (%s=%s): %.2f. Diferencia: %.2f.",
                campoMonto, campoPeriodo, periodoA, totalA,
                campoMonto, campoPeriodo, periodoB, totalB, diff);
    }

    @Tool("Envía un correo electrónico corporativo. Úsalo solo cuando el usuario lo pida explícitamente.")
    public String enviarCorreo(
            @P("dirección del destinatario") String to,
            @P("asunto del correo") String subject,
            @P("cuerpo del correo (texto o HTML)") String body) {
        UUID org = TenantContext.requireOrganizationId();
        UUID user = TenantContext.currentUserId();
        sendEmail.send(new SendEmailUseCase.SendEmailCommand(to, subject, body, org, user));
        return "Correo enviado correctamente a " + to + ".";
    }

    private double sum(UUID org, String campoPeriodo, String periodo, String campoMonto) {
        List<FinancialRecord> records = financialQuery.query(
                new QueryCommand(org, null, campoPeriodo, periodo, 1000));
        double total = 0;
        for (FinancialRecord r : records) {
            Double value = r.getNumericField(campoMonto);
            if (value != null) total += value;
        }
        return total;
    }
}
