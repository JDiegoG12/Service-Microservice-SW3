package com.sw3.service_microservice.config.exception.estructura;

/**
 * Clase utilitaria (Factory) encargada de la construcción de objetos de respuesta de error.
 * <p>
 * Su propósito es centralizar la lógica de instanciación de la clase {@link Error},
 * reduciendo la duplicidad de código en los manejadores de excepciones y asegurando
 * que los objetos de error se inicialicen de manera consistente.
 * </p>
 * <p>
 * Al ser una clase de utilidades, no debe ser instanciada.
 * </p>
 */
public final class ErrorUtils {

    /**
     * Constructor por defecto con visibilidad de paquete para prevenir la instanciación externa.
     */
    ErrorUtils() {
    }

    /**
     * Fábrica estática que crea y popula una nueva instancia del DTO de error.
     *
     * @param codigoError  El código interno de identificación del error (ej: "GC-0001").
     * @param llaveMensaje El mensaje descriptivo del error o la llave de traducción.
     * @param codigoHttp   El código de estado HTTP asociado (ej: 404, 500).
     * @return Una instancia de {@link Error} con los datos básicos configurados.
     */
    public static Error crearError(final String codigoError, final String llaveMensaje, final Integer codigoHttp) {
        final Error error = new Error();
        error.setCodigoError(codigoError);
        error.setMensaje(llaveMensaje);
        error.setCodigoHttp(codigoHttp);
        return error;
    }
}