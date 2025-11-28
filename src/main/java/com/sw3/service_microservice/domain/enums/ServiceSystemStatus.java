package com.sw3.service_microservice.domain.enums;

/**
 * Define el estado del ciclo de vida de un registro de servicio en el sistema.
 * <p>
 * Este enumerado es la pieza clave para implementar el borrado lógico (<b>Soft Delete</b>)
 * según la regla de negocio <b>RN-HU04-01</b>. Permite ocultar servicios para nuevas
 * transacciones sin eliminarlos físicamente de la base de datos, preservando así la
 * integridad de los registros históricos (como reservas pasadas o reportes).
 * </p>
 * <p>
 * Este estado es distinto de {@link ServiceAvailabilityStatus}, que controla si un servicio
 * activo se puede reservar o no en el frontend.
 * </p>
 */
public enum ServiceSystemStatus {

    /**
     * El servicio es parte del catálogo actual, es visible en la gestión administrativa
     * y, por defecto, se incluye en los listados públicos para clientes (a menos que su
     * estado de disponibilidad sea {@code NO_DISPONIBLE}).
     */
    ACTIVO,

    /**
     * Representa un borrado lógico. El servicio se oculta de las listas generales para clientes
     * y de las operaciones de gestión habituales.
     * <p>
     * Se mantiene en la base de datos únicamente para mantener la integridad referencial
     * de los registros históricos que dependen de él. Un servicio inactivo no puede
     * ser reservado.
     * </p>
     */
    INACTIVO
}