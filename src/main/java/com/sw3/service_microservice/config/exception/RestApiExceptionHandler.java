package com.sw3.service_microservice.config.exception;

import com.sw3.service_microservice.config.exception.estructura.CodigoError;
import com.sw3.service_microservice.config.exception.estructura.Error;
import com.sw3.service_microservice.config.exception.estructura.ErrorUtils;
import com.sw3.service_microservice.config.exception.propias.EntidadNoExisteException;
import com.sw3.service_microservice.config.exception.propias.EntidadYaExisteException;
import com.sw3.service_microservice.config.exception.propias.ReglaNegocioExcepcion;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la API REST (Controller Advice).
 * <p>
 * Esta clase intercepta las excepciones lanzadas en cualquier parte de la capa de controladores
 * y las transforma en respuestas HTTP estandarizadas con un cuerpo JSON consistente
 * (objeto {@link Error}). Esto evita que el cliente reciba trazas de error de Java crudas
 * y asegura que los códigos de estado HTTP sean semánticamente correctos.
 * </p>
 */
@ControllerAdvice
public class RestApiExceptionHandler {

    /**
     * Manejador genérico para cualquier excepción no controlada explícitamente (Fallback).
     * <p>
     * Se utiliza cuando ocurre un error inesperado en el sistema (ej: NullPointerException,
     * fallos de conexión a BD no controlados).
     * </p>
     *
     * @param req La petición HTTP que originó el error.
     * @param ex La excepción capturada.
     * @param locale La configuración regional del cliente.
     * @return Respuesta con estado <b>500 Internal Server Error</b> y código genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleGenericException(final HttpServletRequest req,
                                                        final Exception ex, final Locale locale) {
        final Error error = ErrorUtils
                .crearError(CodigoError.ERROR_GENERICO.getCodigo(),
                        CodigoError.ERROR_GENERICO.getLlaveMensaje(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maneja intentos de crear recursos que violan restricciones de unicidad.
     * <p>
     * Ej: Intentar crear un servicio con un nombre que ya existe y está activo.
     * </p>
     *
     * @param req La petición HTTP.
     * @param ex La excepción {@link EntidadYaExisteException}.
     * @return Respuesta con estado <b>406 Not Acceptable</b> indicando conflicto de recursos.
     */
    @ExceptionHandler(EntidadYaExisteException.class)
    public ResponseEntity<Error> handleEntidadYaExiste(final HttpServletRequest req,
                                                       final EntidadYaExisteException ex) {
        final Error error = ErrorUtils
                .crearError(CodigoError.ENTIDAD_YA_EXISTE.getCodigo(),
                        String.format("%s: %s", CodigoError.ENTIDAD_YA_EXISTE.getLlaveMensaje(),
                                ex.getMessage()),
                        HttpStatus.NOT_ACCEPTABLE.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * Maneja violaciones a las reglas de negocio del dominio.
     * <p>
     * Ej: Intentar poner un servicio en "Disponible" sin tener barberos asignados,
     * o intentar eliminar un servicio con reservas futuras.
     * </p>
     *
     * @param req La petición HTTP.
     * @param ex La excepción {@link ReglaNegocioExcepcion}.
     * @return Respuesta con estado <b>400 Bad Request</b>.
     */
    @ExceptionHandler(ReglaNegocioExcepcion.class)
    public ResponseEntity<Error> handleReglaNegocio(final HttpServletRequest req,
                                                    final ReglaNegocioExcepcion ex, final Locale locale) {
        final Error error = ErrorUtils
                .crearError(CodigoError.VIOLACION_REGLA_DE_NEGOCIO.getCodigo(), 
                        ex.formatException(),
                        HttpStatus.BAD_REQUEST.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja intentos de acceso a recursos que no se encuentran en la base de datos.
     * <p>
     * Ej: Consultar, actualizar o eliminar un servicio por un ID inexistente.
     * </p>
     *
     * @param req La petición HTTP.
     * @param ex La excepción {@link EntidadNoExisteException}.
     * @return Respuesta con estado <b>404 Not Found</b>.
     */
    @ExceptionHandler(EntidadNoExisteException.class)
    public ResponseEntity<Error> handleEntidadNoExiste(final HttpServletRequest req,
                                                       final EntidadNoExisteException ex, final Locale locale) {
        final Error error = ErrorUtils
                .crearError(CodigoError.ENTIDAD_NO_ENCONTRADA.getCodigo(),
                        String.format("%s: %s",
                                CodigoError.ENTIDAD_NO_ENCONTRADA.getLlaveMensaje(),
                                ex.getMessage()),
                        HttpStatus.NOT_FOUND.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja errores de validación de campos en los DTOs de entrada (@Valid).
     * <p>
     * Captura excepciones lanzadas por anotaciones como {@code @NotNull}, {@code @Min},
     * {@code @Pattern}, etc. Concatena todos los errores de campo en un solo mensaje legible.
     * </p>
     *
     * @param req La petición HTTP.
     * @param ex La excepción que contiene los errores de validación.
     * @return Respuesta con estado <b>400 Bad Request</b> y lista de campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleValidationExceptions(final HttpServletRequest req, 
                                                            MethodArgumentNotValidException ex) {
        // Concatenamos todos los errores de campos en un solo string
        String mensajes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        final Error error = ErrorUtils
                .crearError(CodigoError.VALIDACION_DTO.getCodigo(),
                        mensajes,
                        HttpStatus.BAD_REQUEST.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de formato en el JSON de entrada (Deserialización).
     * <p>
     * Ocurre típicamente cuando se envía un tipo de dato incorrecto, por ejemplo,
     * enviar texto ("abc") en un campo que espera un número (precio o ID).
     * </p>
     *
     * @param req La petición HTTP.
     * @param ex La excepción de lectura del mensaje HTTP.
     * @return Respuesta con estado <b>400 Bad Request</b> y un mensaje explicativo sobre el formato.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Error> handleTypeMismatch(final HttpServletRequest req, 
                                                    HttpMessageNotReadableException ex) {

        final Error error = ErrorUtils
                .crearError(CodigoError.VALIDACION_DTO.getCodigo(),
                        "Error en el formato de los datos. Verifique que los campos numéricos (precio, duración, id) no contengan texto.",
                        HttpStatus.BAD_REQUEST.value())
                .setUrl(req.getRequestURL().toString()).setMetodo(req.getMethod());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}