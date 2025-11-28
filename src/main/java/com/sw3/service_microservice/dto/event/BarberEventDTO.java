package com.sw3.service_microservice.dto.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO que representa un evento de dominio relacionado con un Barbero.
 * <p>
 * Esta clase define la estructura del mensaje (contrato) que se recibe desde el
 * microservicio de Barberos a través de RabbitMQ.
 * </p>
 * <p>
 * Su propósito es transportar toda la información necesaria para que este microservicio (Servicios)
 * pueda:
 * <ol>
 *     <li>Mantener actualizada su réplica local de barberos (la entidad {@link com.sw3.service_microservice.domain.BarberEntity}).</li>
 *     <li>Sincronizar de forma bidireccional la relación entre barberos y servicios.</li>
 * </ol>
 * Es procesado por el listener {@link com.sw3.service_microservice.access.event.BarberEventListener}.
 * </p>
 */
@Data
@NoArgsConstructor
public class BarberEventDTO {

    /**
     * Identificador único del barbero, originado en el microservicio de Barberos.
     */
    private Long id;

    /**
     * Nombre completo del barbero.
     */
    private String name;

    /**
     * Correo electrónico del barbero.
     */
    private String email; 

    /**
     * Indica el estado del ciclo de vida del barbero en el sistema de origen.
     * <p>
     * {@code true} si está activo (ej. tiene contrato) y puede ser asignado a servicios.
     * {@code false} si ha sido inactivado.
     * </p>
     */
    private Boolean active;

    /**
     * Lista de identificadores de los servicios que este barbero está autorizado a realizar.
     * <p>
     * Este campo es fundamental para la sincronización bidireccional. Cuando un administrador
     * asigna servicios a un barbero en el otro microservicio, esta lista se envía para que
     * el microservicio de Servicios actualice su tabla de relaciones {@code service_barbers}
     * y mantenga la consistencia.
     * </p>
     */
    private List<Long> serviceIds; 
}