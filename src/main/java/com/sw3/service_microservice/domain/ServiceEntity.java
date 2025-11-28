package com.sw3.service_microservice.domain;

import com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus;
import com.sw3.service_microservice.domain.enums.ServiceSystemStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Representa la entidad de dominio principal para un servicio ofrecido por la barbería.
 * <p>
 * Esta clase es el "Aggregate Root" del contexto de servicios. Contiene toda la información
 * comercial, de clasificación, operativa y de estado de un servicio. Está mapeada a la tabla
 * {@code services} en la base de datos.
 * </p>
 */
@Data
@Entity
@Table(name = "services")
public class ServiceEntity {

    /**
     * Identificador único del servicio, generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre comercial del servicio (ej. "Corte Clásico").
     * <p>
     * La restricción {@code unique = true} asegura que no haya dos servicios con el mismo nombre,
     * cumpliendo la regla de negocio <b>RN-HU01-03</b>.
     * </p>
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Descripción detallada del servicio para informar al cliente sobre lo que incluye.
     */
    @Column(nullable = false)
    private String description;

    /**
     * Precio unitario del servicio.
     * <p>
     * Se utiliza {@code BigDecimal} para garantizar la precisión en los cálculos monetarios
     * y evitar los errores de punto flotante asociados con {@code double} o {@code float}.
     * </p>
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * Duración estimada del servicio, expresada en minutos.
     */
    @Column(nullable = false)
    private Integer duration;

    /**
     * Relación 'Muchos a Uno' con la entidad {@link CategoryEntity}.
     * <p>
     * Indica que un servicio pertenece a una única categoría. La columna {@code category_id}
     * en la tabla {@code services} actúa como la clave foránea. No puede ser nula.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    /**
     * Relación 'Muchos a Muchos' con la entidad {@link BarberEntity}.
     * <p>
     * Define qué barberos están calificados o asignados para realizar este servicio.
     * Esta relación se gestiona a través de una tabla intermedia llamada {@code service_barbers}.
     * </p>
     */
    @ManyToMany
    @JoinTable(
        name = "service_barbers",
        joinColumns = @JoinColumn(name = "service_id"),
        inverseJoinColumns = @JoinColumn(name = "barber_id")
    )
    private List<BarberEntity> barbers;

    /**
     * Define el estado de disponibilidad comercial del servicio.
     * <p>
     * Controla si el servicio puede ser reservado por los clientes en el frontend.
     * Es gestionado automáticamente por la regla de negocio <b>RN-HU02-03</b> (depende de si
     * tiene barberos asignados).
     * </p>
     * @see ServiceAvailabilityStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceAvailabilityStatus availabilityStatus;

    /**
     * Define el estado del ciclo de vida del registro en la base de datos.
     * <p>
     * Es la clave para la implementación del borrado lógico (<b>Soft Delete</b>) según la regla <b>RN-HU04-01</b>.
     * Permite ocultar un servicio sin eliminarlo físicamente, preservando el historial.
     * </p>
     * @see ServiceSystemStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceSystemStatus systemStatus;
}