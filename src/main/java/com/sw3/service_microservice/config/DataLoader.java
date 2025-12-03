package com.sw3.service_microservice.config;

import com.sw3.service_microservice.access.event.ServiceEventPublisher;
import com.sw3.service_microservice.domain.CategoryEntity;
import com.sw3.service_microservice.domain.ReservationEntity; 
import com.sw3.service_microservice.domain.ServiceEntity;
import com.sw3.service_microservice.domain.enums.ReservationStatus;
import com.sw3.service_microservice.dto.response.ServiceResponseDTO;
import com.sw3.service_microservice.mapper.ServiceMapper;
import com.sw3.service_microservice.repository.CategoryRepository;
import com.sw3.service_microservice.repository.ReservationRepository;
import com.sw3.service_microservice.repository.ServiceRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;     
    private final ReservationRepository reservationRepository;
    private final ServiceEventPublisher serviceEventPublisher;
    private final ServiceMapper serviceMapper;

    @Override
    public void run(String... args) throws Exception {
        // Solo cargar si la base de datos está vacía
        if (categoryRepository.count() > 0) {
            System.out.println("✅ La base de datos ya tiene datos. Omitiendo DataLoader.");
            return;
        }

        System.out.println("🔄 Inicializando base de datos con datos de prueba...");

        // ============================================================
        // 1. CREAR CATEGORÍAS
        // ============================================================
        CategoryEntity corteCategory = new CategoryEntity();
        corteCategory.setName("Cortes de Cabello");
        categoryRepository.save(corteCategory);

        CategoryEntity barberíaCategory = new CategoryEntity();
        barberíaCategory.setName("Barbería Tradicional");
        categoryRepository.save(barberíaCategory);

        CategoryEntity tratamientoCategory = new CategoryEntity();
        tratamientoCategory.setName("Tratamientos");
        categoryRepository.save(tratamientoCategory);

        System.out.println("✅ Categorías creadas: " + categoryRepository.count());

        // ============================================================
        // 2. CREAR SERVICIOS
        // ============================================================

        // --- Categoría: Cortes de Cabello ---
        ServiceEntity corteClasico = new ServiceEntity();
        corteClasico.setName("Corte Clásico");
        corteClasico.setDescription("Corte tradicional con máquina y tijera");
        corteClasico.setPrice(new java.math.BigDecimal("15000"));
        corteClasico.setDuration(30);
        //corteClasico.setImageUrl("corte-clasico.jpg");
        corteClasico.setCategory(corteCategory);
        corteClasico.setAvailabilityStatus(com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus.DISPONIBLE);
        corteClasico.setSystemStatus(com.sw3.service_microservice.domain.enums.ServiceSystemStatus.ACTIVO);
        serviceRepository.save(corteClasico);

        ServiceEntity corteModerno = new ServiceEntity();
        corteModerno.setName("Corte Moderno");
        corteModerno.setDescription("Corte actualizado con degradado y texturizado");
        corteModerno.setPrice(new java.math.BigDecimal("20000"));
        corteModerno.setDuration(45);
        //corteModerno.setImageUrl("corte-moderno.jpg");
        corteModerno.setCategory(corteCategory);
        corteModerno.setAvailabilityStatus(com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus.DISPONIBLE);
        corteModerno.setSystemStatus(com.sw3.service_microservice.domain.enums.ServiceSystemStatus.ACTIVO);
        serviceRepository.save(corteModerno);

        ServiceEntity corteNiño = new ServiceEntity();
        corteNiño.setName("Corte Niño");
        corteNiño.setDescription("Corte especial para niños");
        corteNiño.setPrice(new java.math.BigDecimal("12000"));
        corteNiño.setDuration(25);
        //corteNiño.setImageUrl("corte-nino.jpg");
        corteNiño.setCategory(corteCategory);
        corteNiño.setAvailabilityStatus(com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus.DISPONIBLE);
        corteNiño.setSystemStatus(com.sw3.service_microservice.domain.enums.ServiceSystemStatus.ACTIVO);
        serviceRepository.save(corteNiño);

        // --- Categoría: Barbería Tradicional ---
        ServiceEntity afeitadoClasico = new ServiceEntity();
        afeitadoClasico.setName("Afeitado Clásico");
        afeitadoClasico.setDescription("Afeitado tradicional con navaja y toalla caliente");
        afeitadoClasico.setPrice(new java.math.BigDecimal("18000"));
        afeitadoClasico.setDuration(30);
        //afeitadoClasico.setImageUrl("afeitado-clasico.jpg");
        afeitadoClasico.setCategory(barberíaCategory);
        afeitadoClasico.setAvailabilityStatus(com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus.DISPONIBLE);
        afeitadoClasico.setSystemStatus(com.sw3.service_microservice.domain.enums.ServiceSystemStatus.ACTIVO);
        serviceRepository.save(afeitadoClasico);

        ServiceEntity arregloBarba = new ServiceEntity();
        arregloBarba.setName("Arreglo de Barba");
        arregloBarba.setDescription("Perfilado y arreglo de barba con máquina y tijera");
        arregloBarba.setPrice(new java.math.BigDecimal("12000"));
        arregloBarba.setDuration(20);
        //arregloBarba.setImageUrl("arreglo-barba.jpg");
        arregloBarba.setCategory(barberíaCategory);
        arregloBarba.setAvailabilityStatus(com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus.DISPONIBLE);
        arregloBarba.setSystemStatus(com.sw3.service_microservice.domain.enums.ServiceSystemStatus.ACTIVO);
        serviceRepository.save(arregloBarba);

        ServiceEntity corteBarba = new ServiceEntity();
        corteBarba.setName("Corte + Barba");
        corteBarba.setDescription("Servicio completo de corte de cabello y arreglo de barba");
        corteBarba.setPrice(new java.math.BigDecimal("28000"));
        corteBarba.setDuration(60);
        //corteBarba.setImageUrl("corte-barba.jpg");
        corteBarba.setCategory(barberíaCategory);
        corteBarba.setAvailabilityStatus(com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus.DISPONIBLE);
        corteBarba.setSystemStatus(com.sw3.service_microservice.domain.enums.ServiceSystemStatus.ACTIVO);
        serviceRepository.save(corteBarba);

        // --- Categoría: Tratamientos ---
        ServiceEntity masajeCapilar = new ServiceEntity();
        masajeCapilar.setName("Masaje Capilar");
        masajeCapilar.setDescription("Masaje relajante de cuero cabelludo");
        masajeCapilar.setPrice(new java.math.BigDecimal("10000"));
        masajeCapilar.setDuration(15);
        //masajeCapilar.setImageUrl("masaje-capilar.jpg");
        masajeCapilar.setCategory(tratamientoCategory);
        masajeCapilar.setAvailabilityStatus(com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus.DISPONIBLE);
        masajeCapilar.setSystemStatus(com.sw3.service_microservice.domain.enums.ServiceSystemStatus.ACTIVO);
        serviceRepository.save(masajeCapilar);

        ServiceEntity tratamientoFacial = new ServiceEntity();
        tratamientoFacial.setName("Tratamiento Facial");
        tratamientoFacial.setDescription("Limpieza facial profunda e hidratación");
        tratamientoFacial.setPrice(new java.math.BigDecimal("25000"));
        tratamientoFacial.setDuration(40);
        //tratamientoFacial.setImageUrl("tratamiento-facial.jpg");
        tratamientoFacial.setCategory(tratamientoCategory);
        tratamientoFacial.setAvailabilityStatus(com.sw3.service_microservice.domain.enums.ServiceAvailabilityStatus.DISPONIBLE);
        tratamientoFacial.setSystemStatus(com.sw3.service_microservice.domain.enums.ServiceSystemStatus.ACTIVO);
        serviceRepository.save(tratamientoFacial);

        System.out.println("✅ Servicios creados: " + serviceRepository.count());
        
        // ============================================================
        // 3. PUBLICAR EVENTOS DE SERVICIOS CREADOS
        // ============================================================
        System.out.println("🔄 Publicando eventos de servicios creados...");
        
        List<ServiceEntity> allServices = Arrays.asList(
            corteClasico, corteModerno, corteNiño,
            afeitadoClasico, arregloBarba, corteBarba,
            masajeCapilar, tratamientoFacial
        );
        
        publishServiceEvents(allServices);
        
        System.out.println("✅ Eventos publicados: " + allServices.size());
        System.out.println("🎉 Base de datos inicializada correctamente!");
    }
    
    /**
     * Publica eventos de creación de servicios para sincronizar otros microservicios.
     * @param services Lista de servicios a publicar
     */
    private void publishServiceEvents(List<ServiceEntity> services) {
        for (ServiceEntity service : services) {
            ServiceResponseDTO dto = serviceMapper.toResponseDTO(service);
            serviceEventPublisher.publishServiceCreated(dto);
            System.out.println("   📤 Evento publicado: " + service.getName() + " (ID: " + service.getId() + ")");
        }
    }
}