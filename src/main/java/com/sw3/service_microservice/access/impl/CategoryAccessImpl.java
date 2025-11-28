package com.sw3.service_microservice.access.impl;

import com.sw3.service_microservice.access.ICategoryAccess;
import com.sw3.service_microservice.config.exception.propias.EntidadYaExisteException;
import com.sw3.service_microservice.domain.CategoryEntity;
import com.sw3.service_microservice.dto.request.CreateCategoryRequestDTO;
import com.sw3.service_microservice.dto.response.CategoryResponseDTO;
import com.sw3.service_microservice.mapper.CategoryMapper;
import com.sw3.service_microservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación concreta de la lógica de negocio para la gestión de Categorías.
 * <p>
 * Esta clase orquesta las operaciones entre el repositorio de datos y los objetos de transferencia (DTO),
 * asegurando que se cumplan las reglas de integridad (como nombres únicos) antes de persistir
 * la información.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CategoryAccessImpl implements ICategoryAccess {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Registra una nueva categoría en la base de datos aplicando reglas de validación.
     * <p>
     * Verifica explícitamente si ya existe una categoría con el mismo nombre (sensible a mayúsculas/minúsculas
     * según la configuración de la BD) para evitar duplicados.
     * </p>
     *
     * @param request DTO con el nombre de la categoría a crear.
     * @return El DTO de la categoría persistida, incluyendo su ID generado.
     * @throws EntidadYaExisteException Si se intenta crear una categoría con un nombre que ya existe en el sistema.
     */
    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CreateCategoryRequestDTO request) {
        // Validación: Nombre único
        if (categoryRepository.existsByName(request.getName())) {
            throw new EntidadYaExisteException("La categoría ya existe: " + request.getName());
        }

        CategoryEntity entity = new CategoryEntity();
        entity.setName(request.getName());

        CategoryEntity saved = categoryRepository.save(entity);

        return categoryMapper.toResponseDTO(saved);
    }

    /**
     * Recupera todas las categorías registradas en el sistema.
     * <p>
     * Este método está marcado como {@code readOnly = true} para optimizar el rendimiento
     * de la transacción en la base de datos, ya que no realiza modificaciones.
     * </p>
     *
     * @return Lista de DTOs de todas las categorías existentes.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}