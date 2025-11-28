package com.sw3.service_microservice.config.exception.estructura;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Modelo que define la estructura estandarizada del cuerpo de respuesta (JSON)
 * para los errores de la API.
 * <p>
 * Esta clase actúa como un DTO (Data Transfer Object) para transportar la información
 * de la excepción hacia el cliente de una manera limpia y uniforme.
 * </p>
 * <p>
 * Ejemplo de JSON resultante:
 * <pre>
 * {
 *   "codigoError": "GC-0004",
 *   "mensaje": "Regla de negocio violada - El precio no puede ser negativo",
 *   "codigoHttp": 400,
 *   "url": "/api/services",
 *   "metodo": "POST"
 * }
 * </pre>
 * </p>
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Error {

    /**
     * Código interno de error de la aplicación (ej: "GC-0001").
     * Permite identificar el tipo de error programáticamente.
     * @see CodigoError
     */
    private String codigoError;

    /**
     * Descripción detallada y legible del error.
     * Puede contener el mensaje de la excepción original o un mensaje personalizado para el usuario.
     */
    private String mensaje;

    /**
     * Valor numérico del estado HTTP (ej: 404, 400, 500).
     * Se incluye en el cuerpo para facilitar el parsing en clientes que ignoran los headers.
     */
    private Integer codigoHttp;

    /**
     * La URI o endpoint específico donde ocurrió la excepción.
     * Útil para depuración y logs en el lado del cliente.
     */
    @Accessors(chain = true)
    private String url;

    /**
     * El verbo HTTP de la petición que originó el error (GET, POST, PUT, DELETE, etc.).
     */
    @Accessors(chain = true)
    private String metodo;
}