package com.sw3.service_microservice.config.exception.propias;

import com.sw3.service_microservice.config.exception.estructura.CodigoError;
import lombok.Getter;

@Getter
public class ReglaNegocioExcepcion extends RuntimeException {

    private final String llaveMensaje;
    private final String codigo;

    public ReglaNegocioExcepcion(final String message) {
        super(message);
        this.llaveMensaje = CodigoError.VIOLACION_REGLA_DE_NEGOCIO.getLlaveMensaje();
        this.codigo = CodigoError.VIOLACION_REGLA_DE_NEGOCIO.getCodigo();
    }
    
    // Método helper para formatear el mensaje en el handler
    public String formatException() {
        return String.format("%s - %s", llaveMensaje, getMessage());
    }
}