package com.sw3.service_microservice.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ServiceResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private Long categoryId; // Solo el ID, como pide el front
    private List<String> barberIds; // Lista de IDs de los barberos
    private String availabilityStatus; // "Disponible" | "No Disponible"
    private String systemStatus;       // "Activo" | "Inactivo"
}