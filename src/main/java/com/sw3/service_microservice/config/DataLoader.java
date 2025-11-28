package com.sw3.service_microservice.config;

import com.sw3.service_microservice.domain.CategoryEntity;
import com.sw3.service_microservice.domain.ReservationEntity; 
import com.sw3.service_microservice.domain.ServiceEntity;
import com.sw3.service_microservice.domain.enums.ReservationStatus;
import com.sw3.service_microservice.repository.CategoryRepository;
import com.sw3.service_microservice.repository.ReservationRepository;
import com.sw3.service_microservice.repository.ServiceRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;     
    private final ReservationRepository reservationRepository;

    @Override
    public void run(String... args) throws Exception {
        // Cargar Categorías de prueba
        if (categoryRepository.count() == 0) {
            CategoryEntity corte = new CategoryEntity();
            corte.setName("Corte Caballero");
            
            CategoryEntity barba = new CategoryEntity();
            barba.setName("Barba y Bigote");
            
            categoryRepository.saveAll(Arrays.asList(corte, barba));
            System.out.println("Categorías cargadas");
        }

        
        if (serviceRepository.count() == 0 && categoryRepository.count() > 0) {
            CategoryEntity cat = categoryRepository.findAll().get(0);
            
            ServiceEntity service = new ServiceEntity();
            service.setName("Servicio con Reserva");
            service.setDescription("Intenta borrarme si puedes");
            service.setPrice(new java.math.BigDecimal("30000"));
            service.setDuration(45);
            service.setCategory(cat);
            service.setAvailabilityStatus(com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus.DISPONIBLE);
            service.setSystemStatus(com.sw3.service_microservice.domain.enums.ServiceSystemStatus.ACTIVO);
            
            ServiceEntity savedService = serviceRepository.save(service);

            // Crear Reserva Bloqueante (EN_ESPERA)
            ReservationEntity reserva = new ReservationEntity();
            reserva.setId(999L);
            reserva.setStart(LocalDateTime.now().plusDays(1)); 
            reserva.setStatus(ReservationStatus.EN_ESPERA);
            reserva.setService(savedService);

            reservationRepository.save(reserva);
        }
    }
}