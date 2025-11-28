package com.sw3.service_microservice.domain;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Representa la entidad de una Categoría de servicio en la base de datos.
 * <p>
 * Las categorías se utilizan para agrupar y clasificar los servicios ofrecidos
 * por la barbería (ej: "Corte de Cabello", "Tratamientos de Barba", "Faciales").
 * </p>
 * <p>
 * Esta entidad tiene una relación de <b>Muchos a Uno</b> con {@link ServiceEntity},
 * lo que significa que un servicio pertenece a una única categoría, pero una
 * categoría puede englobar a muchos servicios.
 * </p>
 */
@Data
@Entity
@Table(name = "categories")
public class CategoryEntity {

    /**
     * Identificador único de la categoría, generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo y único de la categoría.
     * <p>
     * La restricción {@code unique = true} a nivel de base de datos asegura que no puedan
     * existir dos categorías con el mismo nombre, manteniendo así la consistencia
     * y evitando ambigüedades en la clasificación de los servicios.
     * </p>
     */
    @Column(nullable = false, unique = true)
    private String name;
}