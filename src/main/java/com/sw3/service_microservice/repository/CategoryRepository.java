package com.sw3.service_microservice.repository;

import com.sw3.service_microservice.domain.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    
    boolean existsByName(String name);
}