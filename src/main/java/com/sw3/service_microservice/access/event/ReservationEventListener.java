package com.sw3.service_microservice.access.event;

import com.sw3.service_microservice.config.RabbitMqConfig;
import com.sw3.service_microservice.domain.ReservationEntity;
import com.sw3.service_microservice.domain.ServiceEntity;
import com.sw3.service_microservice.domain.enums.ReservationStatus;
import com.sw3.service_microservice.dto.event.ReservationEventDTO;
import com.sw3.service_microservice.repository.ReservationRepository;
import com.sw3.service_microservice.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Componente encargado de escuchar y procesar los eventos emitidos por el microservicio de Reservas.
 * <p>
 * Este listener mantiene una <b>réplica local de solo lectura</b> de las reservas en la base de datos
 * de este microservicio. El propósito de esta réplica es permitir la validación eficiente
 * de la regla de negocio <b>RN-HU04-03</b> (Restricción por Reservas Futuras) sin necesidad de
 * realizar peticiones HTTP síncronas al microservicio de Reservas cada vez que se intenta
 * eliminar un servicio.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventListener {

    private final ReservationRepository reservationRepository;
    private final ServiceRepository serviceRepository;

    /**
     * Procesa los mensajes recibidos en la cola de eventos de reservas.
     * <p>
     * Este método se encarga de:
     * <ol>
     *     <li>Verificar que el servicio asociado a la reserva exista localmente (Integridad Referencial).</li>
     *     <li>Mapear la información del evento (DTO) a la entidad local {@link ReservationEntity}.</li>
     *     <li>Persistir la reserva con su ID original y su estado actualizado.</li>
     * </ol>
     * Si el servicio asociado no se encuentra en la base de datos local (debido a inconsistencia eventual
     * o errores de orden de llegada), el evento es ignorado para evitar guardar datos huérfanos.
     * </p>
     *
     * @param event DTO que contiene los datos de la reserva (ID, fechas, estado, IDs de relación).
     */
    @RabbitListener(queues = RabbitMqConfig.RESERVATION_LISTENER_QUEUE)
    public void handleReservationEvent(ReservationEventDTO event) {
        log.info("Recibido evento de Reserva: ID={}, Estado={}", event.getId(), event.getStatus());

        try {
            // 1. Buscar el servicio asociado
            // Necesitamos la referencia para la Foreign Key
            Optional<ServiceEntity> serviceOpt = serviceRepository.findById(event.getServiceId());

            if (serviceOpt.isEmpty()) {
                log.warn("Reserva recibida para un servicio que no existe localmente (ID Service: {}). Se ignorará.", event.getServiceId());
                return;
            }

            // 2. Mapear DTO -> Entidad
            ReservationEntity entity = new ReservationEntity();
            entity.setId(event.getId()); // ID manual (replica exacta del origen)
            entity.setStart(event.getStart());
            entity.setService(serviceOpt.get());

            // Convertir String a Enum (Manejo de errores si llega un estado desconocido)
            try {
                entity.setStatus(ReservationStatus.valueOf(event.getStatus()));
            } catch (IllegalArgumentException ex) {
                log.error("Estado de reserva desconocido: {}", event.getStatus());
                return;
            }

            // 3. Guardar
            reservationRepository.save(entity);
            log.info("Reserva sincronizada correctamente.");

        } catch (Exception e) {
            log.error("Error al procesar evento de reserva: {}", e.getMessage());
        }
    }
}