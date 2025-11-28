package com.sw3.service_microservice.access.impl;

import com.sw3.service_microservice.access.IServiceAccess;
import com.sw3.service_microservice.access.event.ServiceEventPublisher;
import com.sw3.service_microservice.config.exception.propias.EntidadNoExisteException;
import com.sw3.service_microservice.config.exception.propias.EntidadYaExisteException;
import com.sw3.service_microservice.config.exception.propias.ReglaNegocioExcepcion;
import com.sw3.service_microservice.domain.BarberEntity;
import com.sw3.service_microservice.domain.CategoryEntity;
import com.sw3.service_microservice.domain.ServiceEntity;
import com.sw3.service_microservice.domain.enums.ReservationStatus;
import com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus;
import com.sw3.service_microservice.domain.enums.ServiceSystemStatus;
import com.sw3.service_microservice.dto.request.AssignBarbersRequestDTO;
import com.sw3.service_microservice.dto.request.CreateServiceRequestDTO;
import com.sw3.service_microservice.dto.request.UpdateServiceRequestDTO;
import com.sw3.service_microservice.dto.response.ServiceResponseDTO;
import com.sw3.service_microservice.mapper.ServiceMapper;
import com.sw3.service_microservice.repository.BarberRepository;
import com.sw3.service_microservice.repository.CategoryRepository;
import com.sw3.service_microservice.repository.ReservationRepository;
import com.sw3.service_microservice.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión de Servicios.
 * <p>
 * Esta clase centraliza todas las validaciones, reglas de negocio y persistencia
 * relacionadas con el ciclo de vida de un servicio. Además, coordina la comunicación
 * de eventos hacia el bus de mensajería (RabbitMQ) cuando ocurren cambios importantes.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class ServiceAccessImpl implements IServiceAccess {

    private final ServiceRepository serviceRepository;
    private final CategoryRepository categoryRepository;
    private final BarberRepository barberRepository;
    private final ReservationRepository reservationRepository;
    private final ServiceMapper serviceMapper;
    private final ServiceEventPublisher eventPublisher;

    /**
     * Crea un nuevo servicio o reactiva uno existente previamente eliminado.
     * <p>
     * Lógica aplicada:
     * <ol>
     *     <li><b>Validación de Unicidad (RN-HU01-03):</b> Verifica si el nombre ya existe.</li>
     *     <li><b>Estrategia de Reactivación:</b> Si el servicio existe pero está {@code INACTIVO},
     *     se recicla el registro actualizando sus datos y cambiando su estado a {@code ACTIVO}.</li>
     *     <li><b>Validación de Categoría:</b> Asegura que la categoría asignada exista.</li>
     *     <li><b>Estados Iniciales (RN-HU01-05):</b> Se crea siempre como {@code NO_DISPONIBLE} y {@code ACTIVO}.</li>
     *     <li><b>Publicación:</b> Emite el evento {@code service.created}.</li>
     * </ol>
     * </p>
     *
     * @param request DTO con los datos del nuevo servicio.
     * @return DTO del servicio creado o reactivado.
     * @throws EntidadYaExisteException Si existe un servicio {@code ACTIVO} con el mismo nombre.
     * @throws EntidadNoExisteException Si la categoría indicada no existe.
     */
    @Override
    @Transactional
    public ServiceResponseDTO createService(CreateServiceRequestDTO request) {
        String nameLimpio = request.getName().trim();

        // 1. Buscar si ya existe un servicio con ese nombre (Activo o Inactivo)
        java.util.Optional<ServiceEntity> serviceOpt = serviceRepository.findByNameIgnoreCase(nameLimpio);

        ServiceEntity entity;

        if (serviceOpt.isPresent()) {
            ServiceEntity existingService = serviceOpt.get();

            // CASO A: Existe y está ACTIVO -> Error (Respetamos unicidad)
            if (existingService.getSystemStatus() == ServiceSystemStatus.ACTIVO) {
                throw new EntidadYaExisteException("Ya existe un servicio activo con el nombre: " + nameLimpio);
            }

            // CASO B: Existe pero está INACTIVO -> Reactivación (Reciclaje del ID)
            entity = existingService;

            // Limpiamos barberos antiguos (si tenía) para que nazca sin asignaciones
            if (entity.getBarbers() != null) {
                entity.getBarbers().clear();
            }

        } else {
            // CASO C: No existe -> Creación totalmente nueva
            entity = new ServiceEntity();
            entity.setName(nameLimpio);
        }

        // 2. Validar que la categoría exista
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntidadNoExisteException(
                        "No se encontró la categoría con ID: " + request.getCategoryId()));

        // 3. Actualizar campos
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setDuration(request.getDuration());
        entity.setCategory(category);

        // 4. Asignar Estados Iniciales
        entity.setAvailabilityStatus(ServiceAvailabilityStatus.NO_DISPONIBLE);
        entity.setSystemStatus(ServiceSystemStatus.ACTIVO);

        // 5. Guardar en Base de Datos
        ServiceEntity savedEntity = serviceRepository.save(entity);

        // 6. Convertir a DTO
        ServiceResponseDTO response = serviceMapper.toResponseDTO(savedEntity);

        // 7. Publicar Evento RabbitMQ
        eventPublisher.publishServiceCreated(response);

        return response;
    }

    /**
     * Asigna una lista de barberos a un servicio específico.
     * <p>
     * Reglas aplicadas:
     * <ul>
     *     <li><b>Validación de Integridad:</b> Todos los IDs de barberos deben existir.</li>
     *     <li><b>Filtro de Activos (RN-HU02-02):</b> No se permite asignar barberos inactivos.</li>
     *     <li><b>Disponibilidad Automática (RN-HU02-03):</b> Si la lista tiene elementos, el estado pasa a
     *     {@code DISPONIBLE}. Si la lista queda vacía, pasa a {@code NO_DISPONIBLE}.</li>
     *     <li><b>Publicación:</b> Emite el evento {@code service.updated}.</li>
     * </ul>
     * </p>
     *
     * @param serviceId ID del servicio a modificar.
     * @param request DTO con la lista de IDs de barberos.
     * @return DTO del servicio actualizado.
     */
    @Override
    @Transactional
    public ServiceResponseDTO assignBarbers(Long serviceId, AssignBarbersRequestDTO request) {
        // Buscar el servicio
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntidadNoExisteException("Servicio no encontrado con ID: " + serviceId));

        // Obtener lista de IDs solicitados
        List<Long> barberIds = request.getBarberIds();

        // Buscar los barberos en BD
        List<BarberEntity> barberEntities = barberRepository.findAllById(barberIds);

        // ¿Encontramos todos los barberos solicitados?
        if (barberEntities.size() != barberIds.size()) {
            throw new EntidadNoExisteException("Uno o más barberos indicados no existen en el sistema.");
        }

        // Validar inactivos
        boolean hayInactivos = barberEntities.stream().anyMatch(b -> !b.getActive());
        if (hayInactivos) {
            throw new ReglaNegocioExcepcion("No se pueden asignar barberos inactivos o eliminados.");
        }

        // Asignar (Reemplazo total)
        service.setBarbers(barberEntities);

        // Actualizar estado automáticamente (RN-HU02-03)
        if (!barberEntities.isEmpty()) {
            service.setAvailabilityStatus(ServiceAvailabilityStatus.DISPONIBLE);
        } else {
            service.setAvailabilityStatus(ServiceAvailabilityStatus.NO_DISPONIBLE);
        }

        // Guardar
        ServiceEntity updatedService = serviceRepository.save(service);

        ServiceResponseDTO response = serviceMapper.toResponseDTO(updatedService);

        // Publicar Evento (Es una actualización del servicio)
        eventPublisher.publishServiceUpdated(response);

        return response;
    }

    /**
     * Actualiza la información básica y el estado de un servicio.
     * <p>
     * Reglas aplicadas:
     * <ul>
     *     <li><b>Unicidad (RN-HU03-03):</b> Verifica que el nombre no pertenezca a otro servicio diferente.</li>
     *     <li><b>Consistencia de Estado (RN-HU03-04):</b> Impide cambiar el estado a {@code DISPONIBLE}
     *     si el servicio no tiene barberos asignados actualmente.</li>
     *     <li><b>Publicación:</b> Emite el evento {@code service.updated}.</li>
     * </ul>
     * </p>
     *
     * @param id ID del servicio a actualizar.
     * @param request DTO con los nuevos datos.
     * @return DTO del servicio actualizado.
     */
    @Override
    @Transactional
    public ServiceResponseDTO updateService(Long id, UpdateServiceRequestDTO request) {
        // 1. Buscar servicio
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("Servicio no encontrado con ID: " + id));

        // 2. Limpieza de datos
        String nameLimpio = request.getName().trim();

        // 3. Validación: Nombre único excluyendo el ID actual
        if (serviceRepository.existsByNameIgnoreCaseAndIdNot(nameLimpio, id)) {
            throw new EntidadYaExisteException("Ya existe otro servicio con el nombre: " + nameLimpio);
        }

        // 4. Validación: Categoría
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntidadNoExisteException(
                        "Categoría no encontrada con ID: " + request.getCategoryId()));

        // 5. Validación: Estado "Disponible"
        ServiceAvailabilityStatus nuevoEstado = mapStringToStatus(request.getAvailabilityStatus());

        if (nuevoEstado == ServiceAvailabilityStatus.DISPONIBLE && service.getBarbers().isEmpty()) {
            throw new ReglaNegocioExcepcion(
                    "No se puede cambiar el estado a 'Disponible' porque el servicio no tiene barberos asignados.");
        }

        // 6. Actualización de campos
        service.setName(nameLimpio);
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDuration(request.getDuration());
        service.setCategory(category);
        service.setAvailabilityStatus(nuevoEstado);

        // 7. Guardar
        ServiceEntity updatedService = serviceRepository.save(service);

        ServiceResponseDTO response = serviceMapper.toResponseDTO(updatedService);

        // 8. Publicar Evento RabbitMQ
        eventPublisher.publishServiceUpdated(response);

        return response;
    }

    /**
     * Obtiene el listado de servicios.
     *
     * @param includeInactive Flag para decidir si incluir servicios eliminados lógicamente.
     * @return Lista de servicios.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> getAllServices(boolean includeInactive) {
        List<ServiceEntity> entities;

        if (includeInactive) {
            entities = serviceRepository.findAll();
        } else {
            entities = serviceRepository.findAllBySystemStatus(ServiceSystemStatus.ACTIVO);
        }

        return entities.stream()
                .map(serviceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un servicio por su ID.
     *
     * @param id ID del servicio.
     * @return DTO del servicio.
     * @throws EntidadNoExisteException Si no se encuentra.
     */
    @Override
    @Transactional(readOnly = true)
    public ServiceResponseDTO getServiceById(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("Servicio no encontrado con ID: " + id));

        return serviceMapper.toResponseDTO(service);
    }

    /**
     * Realiza la inactivación (Soft Delete) de un servicio.
     * <p>
     * Pasos del proceso:
     * <ol>
     *     <li><b>Validación de Reservas (RN-HU04-03):</b> Verifica en la réplica local de reservas si existen
     *     citas futuras (En Espera/En Proceso). Si existen, bloquea la eliminación.</li>
     *     <li><b>Desasociación (RN-HU04-02):</b> Elimina la relación con todos los barberos.</li>
     *     <li><b>Soft Delete (RN-HU04-01):</b> Cambia el estado a {@code INACTIVO} y {@code NO_DISPONIBLE}.</li>
     *     <li><b>Publicación:</b> Emite el evento {@code service.inactivated}.</li>
     * </ol>
     * </p>
     *
     * @param id ID del servicio a eliminar.
     * @throws ReglaNegocioExcepcion Si el servicio tiene reservas pendientes.
     */
    @Override
    @Transactional
    public void deleteService(Long id) {
        // 1. Buscar el servicio
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("Servicio no encontrado con ID: " + id));

        // 2. Validación RN-HU04-03: Reservas Futuras
        List<ReservationStatus> estadosBloqueantes = Arrays.asList(
                ReservationStatus.EN_ESPERA,
                ReservationStatus.EN_PROCESO);

        boolean tieneReservasActivas = reservationRepository.existsByServiceAndStatusIn(service, estadosBloqueantes);

        if (tieneReservasActivas) {
            throw new ReglaNegocioExcepcion(
                    "No se puede inactivar el servicio porque tiene reservas futuras pendientes (En Espera o En Proceso). "
                            +
                            "Por favor, cancele o reprograme las reservas primero.");
        }

        // 3. RN-HU04-02: Desasociación
        service.getBarbers().clear();

        // 4. RN-HU04-01: Soft Delete
        service.setSystemStatus(ServiceSystemStatus.INACTIVO);
        service.setAvailabilityStatus(ServiceAvailabilityStatus.NO_DISPONIBLE);

        // 5. Guardar
        serviceRepository.save(service);

        // 6. Publicar Evento de Inactivación
        eventPublisher.publishServiceInactivated(id);
    }

    /**
     * Obtiene solo los IDs de los barberos asociados a un servicio.
     *
     * @param id ID del servicio.
     * @return Lista de IDs de barberos.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Long> getBarbersByServiceId(Long id) {
        // 1. Buscar el servicio
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntidadNoExisteException("Servicio no encontrado con ID: " + id));

        // 2. Obtener los IDs de la lista de barberos
        if (service.getBarbers() == null) {
            return java.util.Collections.emptyList();
        }

        return service.getBarbers().stream()
                .map(BarberEntity::getId)
                .collect(Collectors.toList());
    }

    // Auxiliar
    private ServiceAvailabilityStatus mapStringToStatus(String statusStr) {
        if ("Disponible".equalsIgnoreCase(statusStr)) {
            return ServiceAvailabilityStatus.DISPONIBLE;
        }
        return ServiceAvailabilityStatus.NO_DISPONIBLE;
    }
}