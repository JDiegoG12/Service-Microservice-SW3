package com.sw3.service_microservice.mapper;

import com.sw3.service_microservice.domain.ServiceEntity;
import com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus;
import com.sw3.service_microservice.domain.enums.ServiceSystemStatus;
import com.sw3.service_microservice.dto.response.ServiceResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class ServiceMapper {

    public ServiceResponseDTO toResponseDTO(ServiceEntity entity) {
        if (entity == null) return null;

        ServiceResponseDTO dto = new ServiceResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setDuration(entity.getDuration());
        
        // Mapeo de Categoría (Obtenemos solo el ID)
        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
        }

        // Mapeo de Barberos (Convertimos lista de Entidades a lista de IDs)
        if (entity.getBarbers() != null) {
            dto.setBarberIds(entity.getBarbers().stream()
                    .map(barber -> barber.getId())
                    .collect(Collectors.toList()));
        } else {
            dto.setBarberIds(Collections.emptyList());
        }

        // Traducción de Enums a String formato Frontend
        dto.setAvailabilityStatus(mapAvailability(entity.getAvailabilityStatus()));
        dto.setSystemStatus(mapSystemStatus(entity.getSystemStatus()));

        return dto;
    }

    // Métodos auxiliares para formatear los textos tal cual los pide Angular
    private String mapAvailability(ServiceAvailabilityStatus status) {
        if (status == null) return null;
        switch (status) {
            case DISPONIBLE: return "Disponible";
            case NO_DISPONIBLE: return "No Disponible";
            default: return status.name();
        }
    }

    private String mapSystemStatus(ServiceSystemStatus status) {
        if (status == null) return null;
        switch (status) {
            case ACTIVO: return "Activo";
            case INACTIVO: return "Inactivo";
            default: return status.name();
        }
    }
}