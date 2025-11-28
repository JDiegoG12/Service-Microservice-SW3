package com.sw3.service_microservice.repository;

import com.sw3.service_microservice.domain.BarberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarberRepository extends JpaRepository<BarberEntity, Long> {
}