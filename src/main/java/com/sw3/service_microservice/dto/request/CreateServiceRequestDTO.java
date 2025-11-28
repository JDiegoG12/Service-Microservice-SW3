package com.sw3.service_microservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para encapsular la petición de creación de un nuevo servicio.
 * <p>
 * Define la estructura y las reglas de validación que deben cumplir los datos de entrada
 * enviados por el cliente al crear un servicio. Es la primera barrera de defensa del
 * backend para asegurar la integridad y el formato correcto de los datos.
 * </p>
 * @see com.sw3.service_microservice.controller.ServiceController#create(CreateServiceRequestDTO)
 */
@Data
public class CreateServiceRequestDTO {

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
    @Pattern(regexp = "^[\\p{L}0-9\\s]+$", message = "El nombre de el servicio solo puede contener letras, números y espacios")
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
     * <p>
     * Validación aplicada:
     * <ul>
     *     <li>{@code @NotNull}: Es obligatorio asociar el servicio a una categoría.</li>
     * </ul>
     * </p>
     */
    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    /**
     * Método de validación personalizado que verifica si la duración es un múltiplo de 10.
     * <p>
     * La anotación {@code @AssertTrue} exige que este método retorne {@code true} para que la validación pase.
     * La anotación {@code @JsonIgnore} previene que este campo sea parte del proceso de deserialización
     * del JSON, ya que es un método de lógica y no un campo de entrada.
     * </p>
     *
     * @return {@code true} si la duración es nula o si es un múltiplo de 10, {@code false} en caso contrario.
     */
    @JsonIgnore
    @AssertTrue(message = "La duración debe ser un múltiplo de 10 (ej: 10, 20, 30...)")
    public boolean isDurationMultipleOfTen() {
        if (duration == null) return true; // Se delega la validación de nulo a @NotNull
        return duration % 10 == 0;
    }
}