package com.sw3.service_microservice.config.exception.propias;

import com.sw3.service_microservice.config.exception.estructura.CodigoError;
import lombok.Getter;

@Getter
public class EntidadYaExisteException extends RuntimeException {

    private final String llaveMensaje;
    private final String codigo;

    public EntidadYaExisteException(final String message) {
        super(message);
        this.llaveMensaje = CodigoError.ENTIDAD_YA_EXISTE.getLlaveMensaje();
        this.codigo = CodigoError.ENTIDAD_YA_EXISTE.getCodigo();
    }
}