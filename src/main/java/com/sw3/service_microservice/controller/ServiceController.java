package com.sw3.service_microservice.controller;

import com.sw3.service_microservice.access.IServiceAccess;
import com.sw3.service_microservice.dto.request.AssignBarbersRequestDTO;
import com.sw3.service_microservice.dto.request.CreateServiceRequestDTO;
import com.sw3.service_microservice.dto.request.UpdateServiceRequestDTO;
import com.sw3.service_microservice.dto.response.ServiceResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador REST para gestionar todas las operaciones relacionadas con los servicios de la barbería.
 * <p>
 * Expone los endpoints HTTP para el CRUD (Crear, Leer, Actualizar, Eliminar) de servicios,
 * así como para la gestión de sus relaciones (asignación de barberos).
 * Recibe peticiones, valida los datos de entrada (DTOs) y delega la lógica de negocio
 * a la capa de acceso ({@link IServiceAccess}).
 * </p>
 * Todos los endpoints están bajo la ruta base {@code /api/services}.
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final IServiceAccess serviceAccess;

    /**
     * Endpoint para crear un nuevo servicio.
     * <p>
     * La anotación {@code @Valid} dispara las validaciones definidas en el DTO.
     * </p>
     *
     * @param request El DTO con los datos del servicio a crear.
     * @return Un {@link ResponseEntity} con el DTO del servicio creado y el estado HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<ServiceResponseDTO> create(@Valid @RequestBody CreateServiceRequestDTO request) {
        ServiceResponseDTO response = serviceAccess.createService(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint para actualizar la información de un servicio existente.
     *
     * @param id El identificador único del servicio a actualizar.
     * @param request El DTO con los nuevos datos del servicio.
     * @return Un {@link ResponseEntity} con el DTO del servicio actualizado y el estado HTTP 200 OK.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> update(@PathVariable Long id, 
                                                     @Valid @RequestBody UpdateServiceRequestDTO request) {
        ServiceResponseDTO response = serviceAccess.updateService(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint para asignar o reasignar la lista completa de barberos a un servicio.
     *
     * @param id El identificador único del servicio.
     * @param request El DTO que contiene la lista de IDs de barberos.
     * @return Un {@link ResponseEntity} con el DTO del servicio actualizado (posiblemente con nuevo estado) y el estado HTTP 200 OK.
     */
    @PutMapping("/{id}/barbers")
    public ResponseEntity<ServiceResponseDTO> assignBarbers(@PathVariable Long id, 
                                                            @Valid @RequestBody AssignBarbersRequestDTO request) {
        ServiceResponseDTO response = serviceAccess.assignBarbers(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint para obtener el listado de todos los servicios.
     * Por defecto, solo retorna los servicios con estado de sistema "Activo".
     *
     * @param includeInactive Parámetro de consulta opcional. Si es {@code true}, incluye también los servicios inactivos.
     * @return Un {@link ResponseEntity} con la lista de DTOs de servicios y el estado HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        return ResponseEntity.ok(serviceAccess.getAllServices(includeInactive));
    }

    /**
     * Endpoint para obtener los detalles de un servicio específico por su ID.
     *
     * @param id El identificador único del servicio.
     * @return Un {@link ResponseEntity} con el DTO del servicio encontrado y el estado HTTP 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceAccess.getServiceById(id));
    }

    /**
     * Endpoint para obtener la lista de IDs de los barberos asociados a un servicio específico.
     *
     * @param id El identificador único del servicio.
     * @return Un {@link ResponseEntity} con una lista de Longs (IDs de barberos) y el estado HTTP 200 OK.
     */
     @GetMapping("/{id}/barbers")
    public ResponseEntity<List<Long>> getBarbersByService(@PathVariable Long id) {
        return ResponseEntity.ok(serviceAccess.getBarbersByServiceId(id));
    }

    /**
     * Endpoint para realizar la inactivación (Soft Delete) de un servicio.
     *
     * @param id El identificador único del servicio a inactivar.
     * @return Un {@link ResponseEntity} con el cuerpo vacío y el estado HTTP 204 No Content, indicando éxito.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceAccess.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}