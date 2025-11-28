package com.sw3.service_microservice.mapper;

import com.sw3.service_microservice.domain.CategoryEntity;
import com.sw3.service_microservice.dto.response.CategoryResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponseDTO(CategoryEntity entity) {
        if (entity == null) return null;

        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }
}