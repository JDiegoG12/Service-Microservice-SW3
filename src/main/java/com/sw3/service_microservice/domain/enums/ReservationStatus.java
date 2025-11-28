package com.sw3.service_microservice.domain.enums;

/**
 * Define los posibles estados del ciclo de vida de una reserva en el sistema.
 * <p>
 * Este enumerado representa una réplica de los estados definidos en el microservicio de Reservas.
 * Su propósito principal en este microservicio es permitir la validación de la regla de negocio
 * <b>RN-HU04-03</b>, que impide la inactivación (Soft Delete) de un servicio si tiene reservas
 * en estados que se consideran "activos" o "pendientes".
 * </p>
 */
public enum ReservationStatus {

    /**
     * La cita está confirmada y pendiente de realizarse en el futuro.
     * <b>Este estado impide la inactivación del servicio asociado.</b>
     */
    EN_ESPERA,

    /**
     * El cliente no se presentó a la cita (No-show).
     * Este estado no bloquea la inactivación del servicio.
     */
    INASISTENCIA,

    /**
     * El servicio se está ejecutando actualmente.
     * <b>Este estado impide la inactivación del servicio asociado.</b>
     */
    EN_PROCESO,

    /**
     * El servicio concluyó exitosamente.
     * Este estado no bloquea la inactivación del servicio.
     */
    FINALIZADA,

    /**
     * La cita fue anulada (por el cliente o el administrador) antes de realizarse.
     * Este estado no bloquea la inactivación del servicio.
     */
    CANCELADA
}