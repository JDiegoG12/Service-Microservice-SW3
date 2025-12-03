package com.sw3.service_microservice.access.event;

import com.sw3.service_microservice.config.RabbitMqConfig;
import com.sw3.service_microservice.domain.BarberEntity;
import com.sw3.service_microservice.domain.ServiceEntity;
import com.sw3.service_microservice.dto.event.BarberEventDTO;
import com.sw3.service_microservice.dto.response.ServiceResponseDTO;
import com.sw3.service_microservice.mapper.ServiceMapper;
import com.sw3.service_microservice.repository.BarberRepository;
import com.sw3.service_microservice.repository.ServiceRepository;
import com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Componente encargado de escuchar y procesar los eventos relacionados con los Barberos
 * provenientes del bus de mensajería (RabbitMQ).
 * <p>
 * Su función principal es mantener sincronizada la base de datos local de barberos (tabla espejo)
 * y gestionar la consistencia de las relaciones entre Servicios y Barberos cuando los cambios
 * se originan en el microservicio externo.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BarberEventListener {

    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceEventPublisher serviceEventPublisher;
    private final ServiceMapper serviceMapper; 

    /**
     * Método principal suscrito a la cola de eventos de barberos.
     * Recibe notificaciones de creación, actualización o inactivación de barberos.
     * <p>
     * Realiza una operación de "Upsert" (Crear o Actualizar) sobre la entidad {@link BarberEntity}
     * y delega la sincronización de las relaciones con los servicios.
     * </p>
     *
     * @param event DTO del evento que contiene la información actualizada del barbero y 
     *              la lista de servicios asociados.
     */
    @RabbitListener(queues = RabbitMqConfig.BARBER_LISTENER_QUEUE)
    @Transactional // Importante para manejar las colecciones Lazy y updates dentro de la misma transacción
    public void handleBarberEvent(BarberEventDTO event) {
        log.info("📩 Recibido evento de Barbero: ID={}, Nombre={}, Accion=Sincronizar", 
                event.getId(), event.getName());
        log.info("   ServiceIds en evento: {}", event.getServiceIds());

        try {
            // 1. Sincronizar Datos del Barbero (Tabla Espejo)
            // Se busca por ID; si no existe, se instancia uno nuevo para insertarlo.
            BarberEntity barber = barberRepository.findById(event.getId()).orElse(new BarberEntity());
            boolean isNew = barber.getName() == null;
            
            barber.setId(event.getId());
            barber.setName(event.getName());
            barber.setActive(event.getActive());

            // Guardamos el barbero primero para asegurar que existe en el contexto de persistencia
            barberRepository.save(barber);
            log.info("✅ Barbero {} en BD: {}", isNew ? "creado" : "actualizado", barber.getName());

            // 2. Sincronizar Relaciones (Si el evento incluye la lista de servicios asociados)
            if (event.getServiceIds() != null && !event.getServiceIds().isEmpty()) {
                log.info("🔗 Sincronizando {} servicios especificados en el evento", event.getServiceIds().size());
                syncServices(barber, event.getServiceIds());
            } else {
                // Si es un barbero nuevo sin servicios, asignarle servicios por defecto
                log.info("🎯 Barbero sin servicios asignados, aplicando asignación por defecto");
                assignDefaultServices(barber);
            }

            log.info("✅ Barbero y sus asociaciones sincronizados en BD local.");

        } catch (Exception e) {
            log.error("❌ Error al procesar evento de barbero: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sincroniza la tabla intermedia {@code service_barbers} basándose en la lista de servicios
     * recibida desde el evento del barbero.
     * <p>
     * <b>Importante:</b> Este método actualiza las relaciones guardando directamente en el repositorio.
     * Esto se conoce como "Guardado Silencioso", diseñado para evitar disparar eventos de vuelta
     * a RabbitMQ y prevenir bucles infinitos de mensajería (Ping-Pong).
     * </p>
     *
     * @param barber Entidad del barbero que se está sincronizando.
     * @param newServiceIds Lista de IDs de servicios que deben estar asociados a este barbero.
     */
     private void syncServices(BarberEntity barber, List<Long> newServiceIds) {
        // 1. ASIGNACIÓN: Buscar los servicios que el evento dice que DEBEN tener al barbero
        List<ServiceEntity> serviciosDestino = serviceRepository.findAllById(newServiceIds);

        for (ServiceEntity service : serviciosDestino) {
            // Si el servicio no tiene al barbero, lo agregamos
            if (!service.getBarbers().contains(barber)) {
                service.getBarbers().add(barber);
                
                // REGLA RN-HU02-03: Al agregar, si tiene > 0 barberos, se vuelve DISPONIBLE
                updateAvailability(service);
                
                serviceRepository.save(service);
            }
        }

        // 2. LIMPIEZA: Buscar servicios que tienen al barbero actualmente pero YA NO deberían tenerlo
        List<ServiceEntity> serviciosAntiguos = serviceRepository.findByBarbers_Id(barber.getId());
        
        for (ServiceEntity antiguo : serviciosAntiguos) {
            // Si el servicio antiguo NO está en la lista nueva que llegó, hay que desasociar al barbero
            if (!newServiceIds.contains(antiguo.getId())) {
                antiguo.getBarbers().remove(barber);
                
                // REGLA RN-HU02-03: Al quitar, verificar si quedó vacío para cambiar estado
                updateAvailability(antiguo);
                
                serviceRepository.save(antiguo);
            }
        }
    }

    /**
     * Evalúa y actualiza el estado de disponibilidad de un servicio basado en la cantidad
     * de barberos asignados.
     * <p>
     * Cumple con la regla de negocio: Un servicio solo está disponible
     * si tiene al menos un barbero asociado.
     * </p>
     *
     * @param service La entidad del servicio a evaluar.
     */
    private void updateAvailability(ServiceEntity service) {
        if (!service.getBarbers().isEmpty()) {
            service.setAvailabilityStatus(ServiceAvailabilityStatus.DISPONIBLE);
        } else {
            service.setAvailabilityStatus(ServiceAvailabilityStatus.NO_DISPONIBLE);
        }
    }
    
    /**
     * Asigna servicios por defecto a un barbero nuevo.
     * Estrategia: Cada barbero puede realizar todos los servicios disponibles.
     */
    @Transactional
    private void assignDefaultServices(BarberEntity barber) {
        try {
            log.info("🔄 Iniciando asignación de servicios por defecto para barbero: {}", barber.getName());
            
            // Obtener todos los servicios activos
            List<ServiceEntity> allServices = serviceRepository.findAll();
            
            log.info("📊 Total de servicios disponibles en BD: {}", allServices.size());
            
            if (allServices.isEmpty()) {
                log.warn("⚠️ No hay servicios disponibles para asignar al barbero: {}", barber.getName());
                return;
            }
            
            List<ServiceEntity> servicesModified = new java.util.ArrayList<>();
            
            // Asignar todos los servicios al barbero
            for (ServiceEntity service : allServices) {
                log.debug("  Procesando servicio: {} (ID: {})", service.getName(), service.getId());
                log.debug("  Barberos actuales en el servicio: {}", service.getBarbers().size());
                
                if (!service.getBarbers().contains(barber)) {
                    service.getBarbers().add(barber);
                    updateAvailability(service);
                    servicesModified.add(service);
                    log.debug("  ✅ Barbero agregado al servicio: {}", service.getName());
                } else {
                    log.debug("  ℹ️ Barbero ya estaba asignado al servicio: {}", service.getName());
                }
            }
            
            if (!servicesModified.isEmpty()) {
                log.info("💾 Guardando {} servicios modificados...", servicesModified.size());
                serviceRepository.saveAll(servicesModified);
                serviceRepository.flush(); // Forzar escritura inmediata
                log.info("✅ Asignados {} servicios al barbero: {}", servicesModified.size(), barber.getName());
                
                // Publicar eventos de servicios actualizados para notificar al barber microservice
                log.info("📤 Publicando eventos de servicios actualizados...");
                for (ServiceEntity service : servicesModified) {
                    publishServiceUpdated(service);
                }
                log.info("✅ Eventos publicados exitosamente");
            } else {
                log.warn("⚠️ No se modificó ningún servicio (todos ya tenían al barbero asignado)");
            }
        } catch (Exception e) {
            log.error("❌ Error al asignar servicios por defecto al barbero: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Publica un evento de actualización de servicio con barberos asignados.
     */
    private void publishServiceUpdated(ServiceEntity service) {
        try {
            ServiceResponseDTO dto = serviceMapper.toResponseDTO(service);
            serviceEventPublisher.publishServiceUpdated(dto);
            log.info("📤 Evento de servicio actualizado publicado: {} con {} barberos", 
                    service.getName(), service.getBarbers().size());
        } catch (Exception e) {
            log.error("Error al publicar evento de servicio actualizado: {}", e.getMessage());
        }
    }
}