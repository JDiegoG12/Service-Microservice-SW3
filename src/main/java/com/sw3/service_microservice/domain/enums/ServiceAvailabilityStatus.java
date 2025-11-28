package com.sw3.service_microservice.domain.enums;

/**
 * Define la disponibilidad de un servicio para ser reservado por los clientes en la interfaz.
 * <p>
 * Este estado representa la disponibilidad comercial o de negocio del servicio y está directamente
 * influenciado por la regla de negocio <b>RN-HU02-03</b> (Disponibilidad Condicionada).
 * </p>
 * <p>
 * Es importante distinguirlo de {@link ServiceSystemStatus}, que gestiona el ciclo de vida
 * del registro en la base de datos (activo vs. borrado lógico).
 * </p>
 */
public enum ServiceAvailabilityStatus {

    /**
     * Indica que el servicio está activo y puede ser seleccionado y agendado por los clientes.
     * <p>
     * Generalmente, este estado se activa automáticamente cuando al servicio se le asocia
     * al menos un barbero capaz de realizarlo.
     * </p>
     */
    DISPONIBLE,

    /**
     * Indica que el servicio existe en el catálogo pero no puede ser reservado temporalmente.
     * <p>
     * Este es el estado por defecto para un servicio recién creado (<b>RN-HU01-05</b>) y también
     * se asigna cuando se desasocian todos los barberos de un servicio, o si el administrador
     * lo deshabilita manualmente (por ejemplo, por falta de insumos).
     * </p>
     */
    NO_DISPONIBLE
}