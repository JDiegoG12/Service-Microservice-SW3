package com.sw3.service_microservice.repository;

import com.sw3.service_microservice.domain.ServiceEntity;
import com.sw3.service_microservice.domain.enums.ServiceSystemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Import necesario

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    // Se mantiene para otras validaciones rápidas
    boolean existsByNameIgnoreCase(String name);

    // Se mantiene para el Update
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // Se mantiene para el Listar Activos
    List<ServiceEntity> findAllBySystemStatus(ServiceSystemStatus systemStatus);

    //Buscar la entidad completa por nombre (para la lógica de reactivación)
    Optional<ServiceEntity> findByNameIgnoreCase(String name);

    List<ServiceEntity> findByBarbers_Id(Long barberId);
}