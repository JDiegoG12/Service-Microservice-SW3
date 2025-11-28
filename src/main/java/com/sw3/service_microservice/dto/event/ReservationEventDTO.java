package com.sw3.service_microservice.dto.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que representa un evento de dominio relacionado con una Reserva.
 * <p>
 * Esta clase define la estructura del mensaje (contrato) que se recibe desde el
 * microservicio de Reservas a través de RabbitMQ.
 * </p>
 * <p>
 * Su propósito es transportar la información mínima y necesaria para que este microservicio (Servicios)
 * pueda mantener una réplica local de las reservas (la entidad {@link com.sw3.service_microservice.domain.ReservationEntity})
 * y así validar la regla de negocio <b>RN-HU04-03</b> (impedir la inactivación de servicios
 * con reservas futuras) de forma desacoplada y eficiente.
 * </p>
 * Es procesado por el listener {@link com.sw3.service_microservice.access.event.ReservationEventListener}.
 */
@Data
@NoArgsConstructor
public class ReservationEventDTO {

    /**
     * Identificador único de la reserva, originado en el microservicio de Reservas.
     */
    private Long id;

    /**
     * Identificador del servicio al que está asociada esta reserva.
     * Es la clave para vincular la réplica de la reserva con la entidad
     * {@link com.sw3.service_microservice.domain.ServiceEntity} local.
     */
    private Long serviceId;

    /**
     * Identificador del barbero asignado a esta reserva.
     */
    private Long barberId;

    /**
     * Fecha y hora de inicio de la cita.
     */
    private LocalDateTime start;

    /**
     * Representación en formato String del estado de la reserva (ej: "EN_ESPERA", "CANCELADA").
     * <p>
     * Se recibe como String para mayor flexibilidad en la deserialización. El listener
     * se encarga de convertir este valor al enumerado {@link com.sw3.service_microservice.domain.enums.ReservationStatus}.
     * </p>
     */
    private String status;
}