package com.sw3.service_microservice.access;

import com.sw3.service_microservice.dto.request.AssignBarbersRequestDTO;
import com.sw3.service_microservice.dto.request.CreateServiceRequestDTO;
import com.sw3.service_microservice.dto.request.UpdateServiceRequestDTO;
import com.sw3.service_microservice.dto.response.ServiceResponseDTO;
import java.util.List;

/**
 * Interfaz que define el contrato para la lógica de negocio central de los Servicios de la barbería.
 * <p>
 * Esta capa orquesta todas las operaciones relacionadas con el catálogo de servicios, incluyendo
 * creación, edición, asignación de profesionales (barberos), consultas y el proceso de
 * inactivación segura (Soft Delete).
 * </p>
 */
public interface IServiceAccess {
    
    /**
     * Crea y registra un nuevo servicio en el catálogo.
     * <p>
     * Aplica validaciones de negocio como la unicidad del nombre y la existencia de la categoría.
     * También puede manejar la reactivación de servicios previamente eliminados si el nombre coincide.
     * </p>
     *
     * @param request DTO con la información necesaria para crear el servicio.
     * @return DTO con los detalles del servicio creado, incluyendo su ID y estados iniciales.
     */
    ServiceResponseDTO createService(CreateServiceRequestDTO request);

    /**
     * Gestiona la asociación de barberos a un servicio específico.
     * <p>
     * Este método es crítico para la regla de negocio <b>RN-HU02-03</b>: actualiza automáticamente
     * el estado de disponibilidad del servicio ("Disponible" / "No Disponible") dependiendo de si
     * la lista de barberos asignados queda vacía o no.
     * </p>
     *
     * @param serviceId ID del servicio al cual se le asignarán los barberos.
     * @param request DTO que contiene la lista de IDs de los barberos a asociar.
     * @return DTO del servicio actualizado con el nuevo estado de disponibilidad.
     */
    ServiceResponseDTO assignBarbers(Long serviceId, AssignBarbersRequestDTO request);

    /**
     * Modifica la información de un servicio existente.
     * <p>
     * Valida reglas complejas como evitar duplicados de nombre (excluyendo el propio registro)
     * y asegura que no se pueda cambiar el estado a "Disponible" si el servicio no tiene
     * barberos asignados.
     * </p>
     *
     * @param id ID del servicio a modificar.
     * @param request DTO con los nuevos datos del servicio.
     * @return DTO con la información actualizada del servicio.
     */
    ServiceResponseDTO updateService(Long id, UpdateServiceRequestDTO request);

    /**
     * Recupera el listado de servicios del sistema.
     * <p>
     * Permite filtrar los resultados según el estado del ciclo de vida del servicio.
     * </p>
     *
     * @param includeInactive Si es {@code true}, retorna todos los servicios (Activos e Inactivos).
     *                        Si es {@code false}, retorna únicamente los servicios Activos (visible para clientes).
     * @return Lista de DTOs de servicios que cumplen con el criterio.
     */
    List<ServiceResponseDTO> getAllServices(boolean includeInactive);

    /**
     * Busca y retorna la información detallada de un servicio específico.
     *
     * @param id Identificador único del servicio.
     * @return DTO con los detalles del servicio encontrado.
     * @throws com.sw3.service_microservice.config.exception.propias.EntidadNoExisteException Si el ID no existe.
     */
    ServiceResponseDTO getServiceById(Long id);

    /**
     * Realiza la inactivación (borrado lógico) de un servicio.
     * <p>
     * Este proceso cambia el estado del sistema a "Inactivo" y desasocia a los barberos.
     * Antes de ejecutar, valida la regla <b>RN-HU04-03</b>, impidiendo la acción si el servicio
     * tiene reservas futuras pendientes.
     * </p>
     *
     * @param id ID del servicio a inactivar.
     */
    void deleteService(Long id);

    /**
     * Obtiene el listado de identificadores de los barberos asignados a un servicio.
     * <p>
     * Útil para que el frontend pueda precargar los checkboxes de selección de barberos
     * al editar un servicio.
     * </p>
     *
     * @param id ID del servicio.
     * @return Lista de IDs (Long) de los barberos asociados actualmente.
     */
    List<String> getBarbersByServiceId(Long id);
}