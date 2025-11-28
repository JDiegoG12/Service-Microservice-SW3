package com.sw3.service_microservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración para la infraestructura de mensajería con RabbitMQ.
 * <p>
 * Define la topología de comunicación asíncrona del microservicio, actuando tanto como
 * <b>Productor</b> (publicando eventos de cambios en servicios) como <b>Consumidor</b>
 * (escuchando eventos de Barberos y Reservas para mantener la consistencia eventual).
 * </p>
 */
@Configuration
public class RabbitMqConfig {

    // -------------------------------------------------------------------
    // 1. CONSTANTES (Nombres de Exchanges y Colas)
    // -------------------------------------------------------------------

    /** Nombre del Topic Exchange donde este microservicio publica sus eventos. */
    public static final String SERVICE_EXCHANGE = "service.exchange";

    /** Nombre de la cola local para recibir eventos de Barberos. */
    public static final String BARBER_LISTENER_QUEUE = "service.barber.listener.queue";
    
    /** Nombre de la cola local para recibir eventos de Reservas. */
    public static final String RESERVATION_LISTENER_QUEUE = "service.reservation.listener.queue";

    /** Nombre del Exchange externo del microservicio de Barberos. */
    public static final String BARBER_EXCHANGE = "barber.exchange";
    
    /** Nombre del Exchange externo del microservicio de Reservas. */
    public static final String RESERVATION_EXCHANGE = "reservation.exchange";

    // -------------------------------------------------------------------
    // 2. CONFIGURACIÓN DE JSON (Vital para enviar objetos)
    // -------------------------------------------------------------------

    /**
     * Configura el convertidor de mensajes para usar JSON (Jackson).
     * <p>
     * Esto permite enviar y recibir objetos Java (DTOs) directamente, serializándolos
     * automáticamente a JSON en lugar de enviar bytes crudos.
     * </p>
     *
     * @return El convertidor de mensajes Jackson2Json.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configura la plantilla de RabbitMQ (RabbitTemplate) con el soporte para JSON.
     *
     * @param connectionFactory La fábrica de conexiones inyectada por Spring Boot.
     * @return El template configurado para publicar mensajes.
     */
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // -------------------------------------------------------------------
    // 3. DEFINICIÓN DE NUESTRO EXCHANGE (Producer)
    // -------------------------------------------------------------------

    /**
     * Define el Exchange propio de este microservicio ("service.exchange").
     * <p>
     * Es de tipo <b>Topic</b> para permitir enrutamiento flexible basado en Routing Keys
     * (ej: "service.created", "service.updated").
     * </p>
     *
     * @return El TopicExchange configurado.
     */
    @Bean
    public TopicExchange serviceExchange() {
        return new TopicExchange(SERVICE_EXCHANGE);
    }

    // -------------------------------------------------------------------
    // 4. CONFIGURACIÓN PARA ESCUCHAR BARBEROS (Consumer)
    // -------------------------------------------------------------------

    /**
     * Crea la cola duradera donde llegarán los mensajes del microservicio de Barberos.
     *
     * @return La cola configurada.
     */
    @Bean
    public Queue barberListenerQueue() {
        return new Queue(BARBER_LISTENER_QUEUE, true); // true = durable
    }

    /**
     * Declara el Exchange del microservicio de Barberos.
     * <p>
     * Necesario para poder realizar el binding (enlace). Le dice a RabbitMQ:
     * "Espera que exista este exchange".
     * </p>
     *
     * @return El TopicExchange de Barberos.
     */
    @Bean
    public TopicExchange barberExchange() {
        return new TopicExchange(BARBER_EXCHANGE);
    }

    /**
     * Enlaza (Bind) la cola local con el Exchange de Barberos.
     * <p>
     * Usa la routing key {@code "barber.#"} para escuchar <b>todos</b> los eventos
     * relacionados con barberos (creación, actualización, borrado).
     * </p>
     *
     * @param barberListenerQueue La cola local.
     * @param barberExchange El exchange remoto.
     * @return El Binding configurado.
     */
    @Bean
    public Binding bindingBarberEvents(Queue barberListenerQueue, TopicExchange barberExchange) {
        return BindingBuilder.bind(barberListenerQueue).to(barberExchange).with("barber.#");
    }

    // -------------------------------------------------------------------
    // 5. CONFIGURACIÓN PARA ESCUCHAR RESERVAS (Consumer)
    // -------------------------------------------------------------------

    /**
     * Crea la cola duradera donde llegarán los mensajes del microservicio de Reservas.
     *
     * @return La cola configurada.
     */
    @Bean
    public Queue reservationListenerQueue() {
        return new Queue(RESERVATION_LISTENER_QUEUE, true);
    }

    /**
     * Declara el Exchange del microservicio de Reservas.
     *
     * @return El TopicExchange de Reservas.
     */
    @Bean
    public TopicExchange reservationExchange() {
        return new TopicExchange(RESERVATION_EXCHANGE);
    }

    /**
     * Enlaza la cola local con el Exchange de Reservas.
     * <p>
     * Usa la routing key {@code "reservation.#"} para escuchar todos los eventos
     * de reservas, necesarios para validar la regla de negocio de no borrar servicios con citas futuras.
     * </p>
     *
     * @param reservationListenerQueue La cola local.
     * @param reservationExchange El exchange remoto.
     * @return El Binding configurado.
     */
    @Bean
    public Binding bindingReservationEvents(Queue reservationListenerQueue, TopicExchange reservationExchange) {
        return BindingBuilder.bind(reservationListenerQueue).to(reservationExchange).with("reservation.#");
    }
}