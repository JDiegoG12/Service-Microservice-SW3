package com.sw3.service_microservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * DTO (Data Transfer Object) utilizado para encapsular la petición de asignación
 * de barberos a un servicio específico.
 * <p>
 * Su propósito es transportar la lista completa y definitiva de los identificadores (`ID`)
 * de los barberos que deben quedar asociados a un servicio después de una operación de actualización.
 * Permite tanto agregar/reemplazar barberos como desasociar todos los existentes enviando
 * una lista vacía (`[]`).
 * </p>
 * <p>
 * Es utilizado como el cuerpo (RequestBody) en el endpoint de asignación de barberos.
 * </p>
 * @see com.sw3.service_microservice.controller.ServiceController#assignBarbers(Long, AssignBarbersRequestDTO)
 */
@Data
public class AssignBarbersRequestDTO {

    /**
     * Lista de identificadores únicos (`ID`) de los barberos que quedarán asociados al servicio.
     * <p>
     * La anotación {@code @NotNull} asegura que el campo {@code barberIds} debe estar presente en el
     * JSON de la petición, aunque la lista en sí misma puede estar vacía.
     * </p>
     * <p>
     * Una lista vacía (`[]`) se interpreta como la intención de desasociar a todos los
     * barberos del servicio, lo cual resultará en que el estado del servicio pase a ser
     * "No Disponible" (según la regla de negocio <b>RN-HU02-03</b>).
     * </p>
     */
    @NotNull(message = "La lista de barberos no puede ser nula")
    private List<Long> barberIds;
}