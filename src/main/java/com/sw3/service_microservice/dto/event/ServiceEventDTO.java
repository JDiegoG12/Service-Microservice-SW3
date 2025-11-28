package com.sw3.service_microservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO que representa un evento de dominio relacionado con un Servicio.
 * <p>
 * Esta clase define la estructura del mensaje (contrato) que este microservicio publica
 * hacia el bus de mensajería (RabbitMQ) cuando ocurre un cambio en el catálogo de servicios.
 * </p>
 * <p>
 * Su propósito es notificar a otros microservicios (como Barberos, Reservas, etc.) sobre
 * la creación, actualización o inactivación de un servicio, permitiéndoles sincronizar
 * sus réplicas de datos y mantener la consistencia eventual del sistema.
 * </p>
 * Es construido y enviado por el {@link com.sw3.service_microservice.access.event.ServiceEventPublisher}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEventDTO {

    /**
     * Identificador único del servicio.
     */
    private Long id;

    /**
     * Nombre comercial del servicio.
     */
    private String name;

    /**
     * Descripción detallada del servicio.
     */
    private String description;

    /**
     * Precio del servicio en el momento del evento.
     */
    private BigDecimal price;

    /**
     * Duración estimada del servicio en minutos.
     */
    private Integer duration;

    /**
     * Lista de identificadores de los barberos asociados a este servicio.
     * <p>
     * Este campo es fundamental para la sincronización bidireccional. Permite
     * al microservicio de Barberos saber qué relaciones de {@code barber_services}
     * debe crear o eliminar para mantenerse consistente.
     * </p>
     */
    private List<Long> barberIds; 

    /**
     * Representación en formato String del estado de disponibilidad comercial del servicio
     * (ej: "Disponible", "No Disponible").
     */
    private String availabilityStatus;

    /**
     * Representación en formato String del estado del ciclo de vida del registro
     * (ej: "Activo", "Inactivo"). Esencial para que otros microservicios
     * sepan cuándo realizar un borrado lógico en sus réplicas.
     */
    private String systemStatus;
}