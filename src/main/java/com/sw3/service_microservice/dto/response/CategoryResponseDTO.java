package com.sw3.service_microservice.dto.response;

import lombok.Data;

/**
 * DTO (Data Transfer Object) para representar la respuesta de una Categoría.
 * <p>
 * Esta clase define la estructura de los datos que se envían al cliente (frontend)
 * cuando se consulta una categoría o como resultado de una operación de creación exitosa.
 * </p>
 * <p>
 * Su propósito es exponer únicamente la información relevante y segura de la entidad
 * {@link com.sw3.service_microservice.domain.CategoryEntity}, desacoplando la representación
 * interna de la base de datos de la que se muestra en la API.
 * </p>
 * @see com.sw3.service_microservice.controller.CategoryController
 */
@Data
public class CategoryResponseDTO {

    /**
     * Identificador único de la categoría.
     * Es la clave primaria de la entidad en la base de datos.
     */
    private Long id;

    /**
     * Nombre descriptivo de la categoría.
     */
    private String name;
}