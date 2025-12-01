package com.sw3.service_microservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Representa la entidad de un Barbero dentro del contexto del microservicio de Servicios.
 * <p>
 * Esta entidad actúa como una <b>réplica de datos</b> o "Tabla Espejo". Su propósito es
 * almacenar una copia local y simplificada de la información de los barberos que se
 * gestionan en otro microservicio.
 * </p>
 * <p>
 * Mantener esta réplica local es crucial para:
 * <ol>
 *     <li>Permitir consultas eficientes de las relaciones (ej: qué barberos hacen qué servicio)
 *     sin necesidad de llamadas HTTP síncronas.</li>
 *     <li>Validar reglas de negocio, como impedir la asignación de barberos inactivos (<b>RN-HU02-02</b>).</li>
 * </ol>
 * Los datos de esta tabla son poblados y actualizados de forma asíncrona a través de eventos
 * de RabbitMQ escuchados por {@link com.sw3.service_microservice.access.event.BarberEventListener}.
 * </p>
 */
@Data
@Entity
@Table(name = "barbers")
public class BarberEntity {

    /**
     * Identificador único del barbero.
     * <p>
     * <b>Importante:</b> Este campo no utiliza {@code @GeneratedValue} porque el ID es
     * controlado y asignado por el microservicio "dueño" de los barberos.
     * Este valor se recibe a través del evento de RabbitMQ para mantener la consistencia
     * entre sistemas.
     * </p>
     */
    @Id
    private String id;

    /**
     * Nombre completo del barbero.
     */
    private String name;

    /**
     * Indica el estado del ciclo de vida del barbero en el sistema de origen.
     * <p>
     * {@code true} si el barbero está activo y puede ser asignado a servicios.
     * {@code false} si el barbero ha sido inactivado o "eliminado lógicamente" en su
     * microservicio de origen. Este campo es usado para la validación <b>RN-HU02-02</b>.
     * </p>
     */
    private Boolean active;
}