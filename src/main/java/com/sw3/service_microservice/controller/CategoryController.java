package com.sw3.service_microservice.controller;

import com.sw3.service_microservice.access.ICategoryAccess;
import com.sw3.service_microservice.dto.request.CreateCategoryRequestDTO;
import com.sw3.service_microservice.dto.response.CategoryResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar las categorías de los servicios (ej: "Corte", "Barba", "Tratamiento").
 * <p>
 * Proporciona los endpoints necesarios para que los administradores puedan crear y consultar
 * las categorías que se usarán para clasificar los servicios ofrecidos por la barbería.
 * </p>
 * Todos los endpoints están bajo la ruta base {@code /api/categories}.
 */
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryAccess categoryAccess;

    /**
     * Endpoint para crear una nueva categoría de servicio.
     * <p>
     * Recibe los datos de la categoría en el cuerpo de la petición y utiliza
     * la anotación {@code @Valid} para asegurar que se cumplan las validaciones
     * definidas en el DTO de entrada.
     * </p>
     *
     * @param request DTO que contiene el nombre de la categoría a crear.
     * @return Un {@link ResponseEntity} con el DTO de la categoría creada y el estado HTTP 201 Created.
     */
    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CreateCategoryRequestDTO request) {
        CategoryResponseDTO response = categoryAccess.createCategory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener el listado completo de todas las categorías disponibles.
     * <p>
     * Este endpoint es comúnmente utilizado por el frontend para poblar menús desplegables
     * o selectores en los formularios de creación y edición de servicios.
     * </p>
     *
     * @return Un {@link ResponseEntity} que contiene una lista de DTOs de categorías y el estado HTTP 200 OK.
     */
    @GetMapping("/public/categories")
    public ResponseEntity<List<CategoryResponseDTO>> getAll() {
        return ResponseEntity.ok(categoryAccess.getAllCategories());
    }
}