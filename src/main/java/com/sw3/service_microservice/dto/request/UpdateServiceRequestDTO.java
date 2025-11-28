package com.sw3.service_microservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para encapsular la petición de actualización de un servicio existente.
 * <p>
 * Define la estructura y las reglas de validación para los datos enviados al modificar
 * la información de un servicio. A diferencia del DTO de creación, este incluye el campo
 * {@code availabilityStatus} para permitir su modificación manual.
 * </p>
 * @see com.sw3.service_microservice.controller.ServiceController#update(Long, UpdateServiceRequestDTO)
 */
@Data
public class UpdateServiceRequestDTO {

    /**
     * Nombre del servicio.
     * <p>
     * Validaciones aplicadas:
     * <ul>
     *     <li>{@code @NotBlank}: No puede ser nulo o vacío.</li>
     *     <li>{@code @Size}: Longitud entre 3 y 50 caracteres.</li>
     *     <li>{@code @Pattern}: Solo permite letras (incluyendo acentos), números y espacios.</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre solo puede contener letras, números y espacios")
    private String name;

    /**
     * Descripción detallada del servicio.
     * <p>
     * Validaciones aplicadas:
     * <ul>
     *     <li>{@code @NotBlank}: No puede ser nulo o vacío.</li>
     *     <li>{@code @Size}: Longitud entre 10 y 200 caracteres.</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 200, message = "La descripción debe tener entre 10 y 200 caracteres")
    private String description;

    /**
     * Precio del servicio.
     * <p>
     * Validaciones aplicadas:
     * <ul>
     *     <li>{@code @NotNull}: No puede ser nulo.</li>
     *     <li>{@code @DecimalMin}: Debe ser como mínimo 1000.00.</li>
     *     <li>{@code @DecimalMax}: Debe ser como máximo 500000.00.</li>
     * </ul>
     * </p>
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "1000.00", message = "El precio mínimo es 1000")
    @DecimalMax(value = "500000.00", message = "El precio máximo es 500.000")
    private BigDecimal price;

    /**
     * Duración del servicio en minutos.
     * <p>
     * Validaciones aplicadas:
     * <ul>
     *     <li>{@code @NotNull}: No puede ser nulo.</li>
     *     <li>{@code @Min}: La duración mínima es de 10 minutos.</li>
     *     <li>{@code @Max}: La duración máxima es de 240 minutos (4 horas).</li>
     * </ul>
     * </p>
     */
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 10, message = "La duración mínima es de 10 minutos")
    @Max(value = 240, message = "La duración máxima es de 4 horas (240 min)")
    private Integer duration;

    /**
     * Identificador único de la categoría a la que pertenece el servicio.
     */
    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    /**
     * El estado de disponibilidad comercial del servicio.
     * <p>
     * Este campo permite al administrador cambiar manualmente si un servicio está disponible
     * para ser reservado, siempre que se cumplan las reglas de negocio (ej: no se puede poner
     * "Disponible" si no hay barberos).
     * <p>
     * Validaciones aplicadas:
     * <ul>
     *     <li>{@code @NotBlank}: El estado es un campo requerido en la actualización.</li>
     *     <li>{@code @Pattern}: Asegura que el valor sea exactamente "Disponible" o "No Disponible".</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(Disponible|No Disponible)$", message = "El estado debe ser 'Disponible' o 'No Disponible'")
    private String availabilityStatus;

    /**
     * Método de validación personalizado que verifica si la duración es un múltiplo de 10.
     *
     * @return {@code true} si la duración es nula o un múltiplo de 10, {@code false} en caso contrario.
     */
    @JsonIgnore
    @AssertTrue(message = "La duración debe ser un múltiplo de 10 (ej: 10, 20, 30...)")
    public boolean isDurationMultipleOfTen() {
        if (duration == null) return true;
        return duration % 10 == 0;
    }
}