package com.sw3.service_microservice.access;

import com.sw3.service_microservice.dto.request.CreateCategoryRequestDTO;
import com.sw3.service_microservice.dto.response.CategoryResponseDTO;

import java.util.List;

/**
 * Interfaz que define el contrato para la lógica de negocio relacionada con las Categorías.
 * <p>
 * Esta capa (Access) actúa como intermediaria entre los controladores REST y los repositorios,
 * encargándose de la validación de reglas de negocio, transformación de datos (DTOs)
 * y orquestación del flujo de información para las categorías de servicios (ej: "Corte", "Barba").
 * </p>
 */
public interface ICategoryAccess {

    /**
     * Registra una nueva categoría en el sistema.
     * <p>
     * Este método debe validar reglas de negocio previas, como verificar que no exista
     * otra categoría con el mismo nombre, antes de persistir la entidad.
     * </p>
     *
     * @param request Objeto DTO con la información necesaria para crear la categoría (nombre).
     * @return Objeto DTO con los detalles de la categoría creada (incluyendo su ID generado).
     */
    CategoryResponseDTO createCategory(CreateCategoryRequestDTO request);

    /**
     * Recupera el listado completo de categorías registradas en el sistema.
     * <p>
     * Utilizado principalmente para poblar selectores (dropdowns) en el frontend
     * al momento de crear o editar servicios.
     * </p>
     *
     * @return Lista de objetos DTO representando todas las categorías existentes.
     */
    List<CategoryResponseDTO> getAllCategories();
}