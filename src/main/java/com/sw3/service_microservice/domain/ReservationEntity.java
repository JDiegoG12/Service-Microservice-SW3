package com.sw3.service_microservice.domain;

import com.sw3.service_microservice.domain.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Representa la entidad de una Reserva dentro del contexto del microservicio de Servicios.
 * <p>
 * Esta entidad es una <b>réplica de datos</b> (Tabla Espejo) que almacena la información
 * mínima y necesaria de las reservas gestionadas en otro microservicio.
 * </p>
 * <p>
 * Su propósito principal es permitir la validación local y eficiente de la regla de negocio
 * <b>RN-HU04-03</b>, que prohíbe la inactivación de un servicio si tiene reservas activas
 * o en proceso, todo ello sin requerir llamadas síncronas (HTTP) al microservicio de Reservas.
 * Los datos de esta tabla son mantenidos por el listener {@link com.sw3.service_microservice.access.event.ReservationEventListener}.
 * </p>
 */
@Data
@Entity
@Table(name = "reservations")
public class ReservationEntity {

    /**
     * Identificador único de la reserva.
     * <p>
     * <b>Importante:</b> Este campo no es autogenerado (no usa {@code @GeneratedValue}).
     * Su valor es una copia exacta del ID proveniente del microservicio de Reservas,
     * recibido a través de un evento de RabbitMQ, para mantener una referencia consistente.
     * </p>
     */
    @Id
    private Long id;

    /**
     * Fecha y hora de inicio de la cita.
     * Este campo es relevante para identificar si una reserva es 'futura'.
     */
    @Column(nullable = false)
    private LocalDateTime start;

    /**
     * Estado actual del ciclo de vida de la reserva.
     * <p>
     * Es el campo clave para la validación de la regla de negocio, utilizado para determinar
     * si una reserva bloquea la eliminación de un servicio. Ver {@link ReservationStatus}.
     * Se almacena como un String en la base de datos para facilitar la legibilidad (ej: "EN_ESPERA").
     * </p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    /**
     * Relación 'Muchos a Uno' con la entidad {@link ServiceEntity}.
     * <p>
     * Establece el vínculo directo con el servicio al que pertenece esta reserva.
     * Es fundamental para poder realizar la consulta de bloqueo de la regla
     * <b>RN-HU04-03</b> de forma eficiente (ej: "buscar reservas para este servicio").
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;
}