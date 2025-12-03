package com.sw3.service_microservice.config.exception.estructura;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Catálogo centralizado de códigos de error de la aplicación.
 * <p>
 * Este enumerado define los códigos internos estandarizados que se devuelven en el cuerpo
 * de las respuestas de error (JSON). Permite al cliente (Frontend u otros microservicios)
 * identificar la naturaleza del problema de forma programática, más allá del código de estado HTTP.
 * </p>
 * <p>
 * Formato típico: "GC-XXXX" (Gestión Central / General Code).
 * </p>
 */
@RequiredArgsConstructor
@Getter
public enum CodigoError {

    /**
     * Error no controlado o inesperado (Fallback).
     * Generalmente asociado a un HTTP 500 Internal Server Error.
     */
    ERROR_GENERICO("GC-0001", "ERROR GENERICO"),

    /**
     * Intento de crear un recurso que viola una restricción de unicidad.
     * Ej: Nombre de servicio duplicado. Asociado a HTTP 406.
     */
    ENTIDAD_YA_EXISTE("GC-0002", "ERROR ENTIDAD YA EXISTE"),

    /**
     * Intento de acceder a un recurso que no existe en la base de datos.
     * Ej: Buscar servicio por ID inválido. Asociado a HTTP 404.
     */
    ENTIDAD_NO_ENCONTRADA("GC-0003", "Entidad no encontrada"),

    /**
     * Violación de una regla de negocio específica del dominio.
     * Ej: Intentar borrar un servicio con reservas futuras. Asociado a HTTP 400.
     */
    VIOLACION_REGLA_DE_NEGOCIO("GC-0004", ""),

    /**
     * Fallo de autenticación.
     * (Reservado para futura implementación de seguridad).
     */
    CREDENCIALES_INVALIDAS("GC-0005", "Error al iniciar sesión, compruebe sus credenciales"),

    /**
     * Usuario inactivo o no verificado.
     * (Reservado para futura implementación de seguridad).
     */
    USUARIO_DESHABILITADO("GC-0006", "El usuario no ha sido verificado"),

    /**
     * Error de validación de campos en los DTOs de entrada.
     * Generado automáticamente por anotaciones como @NotNull, @Size, @Min.
     * Asociado a HTTP 400.
     */
    VALIDACION_DTO("GC-0007", "Error en los datos enviados");

    /** Código alfanumérico único para identificación del error en el sistema. */
    private final String codigo;
    
    /** Mensaje descriptivo por defecto o llave para internacionalización. */
    private final String llaveMensaje;
}