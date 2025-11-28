package com.sw3.service_microservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO (Data Transfer Object) utilizado para encapsular la petición de creación
 * de una nueva categoría de servicio.
 * <p>
 * Su rol principal es actuar como un contrato para la API, definiendo la estructura
 * y las reglas de validación para los datos de entrada al crear una categoría.
 * Es utilizado como el {@code RequestBody} en el endpoint de creación.
 * </p>
 * @see com.sw3.service_microservice.controller.CategoryController#create(CreateCategoryRequestDTO)
 */
@Data
public class CreateCategoryRequestDTO {

    /**
     * El nombre de la categoría.
     * <p>
     * Se aplican las siguientes validaciones:
     * <ul>
     *     <li>{@code @NotBlank}: Asegura que el nombre no sea nulo, vacío o contenga únicamente espacios en blanco.</li>
     *     <li>{@code @Size}: Limita la longitud del nombre a un mínimo de 3 y un máximo de 50 caracteres.</li>
     *     <li>{@code @Pattern}: Restringe el nombre para que solo contenga caracteres alfanuméricos y espacios,
     *     soportando letras de cualquier idioma (incluyendo acentos y tildes gracias a {@code \\p{L}}).</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[\\p{L}0-9\\s]+$", message = "El nombre de la categoría solo puede contener letras, números y espacios")
    private String name;
}