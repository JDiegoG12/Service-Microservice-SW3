package com.sw3.service_microservice.repository;

import com.sw3.service_microservice.domain.ReservationEntity;
import com.sw3.service_microservice.domain.ServiceEntity;
import com.sw3.service_microservice.domain.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    //¿Existe alguna reserva para este servicio que tenga alguno de estos estados?
    boolean existsByServiceAndStatusIn(ServiceEntity service, List<ReservationStatus> statuses);
}