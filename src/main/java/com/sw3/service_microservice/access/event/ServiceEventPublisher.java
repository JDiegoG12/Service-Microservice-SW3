package com.sw3.service_microservice.access.event;

import com.sw3.service_microservice.config.RabbitMqConfig;
import com.sw3.service_microservice.dto.event.ServiceEventDTO;
import com.sw3.service_microservice.dto.response.ServiceResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Componente responsable de publicar eventos de dominio relacionados con la entidad 'Servicio'
 * hacia el bus de mensajería (RabbitMQ).
 * <p>
 * Actúa como el <b>Productor (Producer)</b> en la arquitectura orientada a eventos.
 * Notifica a otros microservicios (como Reservas o Barberos) sobre cambios en el catálogo,
 * permitiendo que estos actualicen sus réplicas de datos o validen reglas de negocio.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica un evento indicando que se ha creado un nuevo servicio en el catálogo.
     * <p>
     * Se envía al Exchange configurado con la Routing Key {@code "service.created"}.
     * </p>
     *
     * @param service DTO con la información completa del servicio recién persistido.
     */
    public void publishServiceCreated(ServiceResponseDTO service) {
        ServiceEventDTO event = mapToEvent(service);
        
        // Publicamos al exchange con la routing key "service.created"
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.SERVICE_EXCHANGE,
                "service.created",
                event
        );
        log.info("Evento publicado: service.created -> ID: {}", service.getId());
    }

    /**
     * Publica un evento indicando que un servicio existente ha sido modificado.
     * <p>
     * Esto incluye cambios en atributos básicos (precio, nombre) o cambios en la
     * asignación de barberos. Se envía con la Routing Key {@code "service.updated"}.
     * </p>
     *
     * @param service DTO con la información actualizada del servicio.
     */
    public void publishServiceUpdated(ServiceResponseDTO service) {
        ServiceEventDTO event = mapToEvent(service);
        
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.SERVICE_EXCHANGE,
                "service.updated",
                event
        );
        log.info("Evento publicado: service.updated -> ID: {}", service.getId());
    }

    /**
     * Publica un evento indicando que un servicio ha sido inactivado (borrado lógico).
     * <p>
     * Se envía un evento ligero que contiene principalmente el ID y los nuevos estados
     * (SystemStatus=Inactivo, AvailabilityStatus=No Disponible) con la Routing Key
     * {@code "service.inactivated"}.
     * </p>
     *
     * @param serviceId Identificador único del servicio que fue inactivado.
     */
    public void publishServiceInactivated(Long serviceId) {
        // Para inactivación, enviamos un evento mínimo
        ServiceEventDTO event = ServiceEventDTO.builder()
                .id(serviceId)
                .systemStatus("Inactivo")
                .availabilityStatus("No Disponible")
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.SERVICE_EXCHANGE,
                "service.inactivated",
                event
        );
        log.info("Evento publicado: service.inactivated -> ID: {}", serviceId);
    }

    /**
     * Convierte el DTO de respuesta de la API (REST) al DTO de evento (Messaging).
     * <p>
     * Este método es crucial para el desacoplamiento: permite que la estructura de la API
     * evolucione independientemente del contrato de mensajes que esperan los otros microservicios.
     * Incluye la lista de {@code barberIds} para mantener la sincronización de relaciones.
     * </p>
     *
     * @param dto El objeto de respuesta de la capa de acceso.
     * @return El objeto de evento listo para ser serializado a JSON.
     */
    private ServiceEventDTO mapToEvent(ServiceResponseDTO dto) {
        return ServiceEventDTO.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .duration(dto.getDuration())
                .barberIds(dto.getBarberIds())
                .availabilityStatus(dto.getAvailabilityStatus())
                .systemStatus(dto.getSystemStatus())
                .build();
    }
}