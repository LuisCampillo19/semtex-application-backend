package com.semtex.domain.port.out;

import com.semtex.domain.model.ParsedSpreadsheet;

/**
 * Puerto de salida: parseo de hojas de cálculo (frontera tecnológica: Apache POI / commons-csv).
 */
public interface SpreadsheetParserPort {

    /**
     * @param content     bytes del archivo subido
     * @param contentType MIME informado por el cliente (puede ser null)
     * @param filename    nombre original (se usa su extensión como respaldo)
     */
    ParsedSpreadsheet parse(byte[] content, String contentType, String filename);
}
