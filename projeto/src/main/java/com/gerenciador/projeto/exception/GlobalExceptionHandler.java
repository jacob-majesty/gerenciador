package com.gerenciador.projeto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe de tratamento global de exceções para a aplicação Spring Boot.
 * Centraliza a lógica de tratamento de erros e retorna respostas HTTP padronizadas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Lida com a exceção {@link ProjectNotFoundException}.
     * Retorna um status HTTP 404 NOT FOUND.
     * @param ex A exceção ProjectNotFoundException.
     * @param request A requisição web.
     * @return Uma ResponseEntity com detalhes do erro.
     */
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleProjectNotFoundException(ProjectNotFoundException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Lida com a exceção {@link InvalidStatusTransitionException}.
     * Retorna um status HTTP 400 BAD REQUEST.
     * @param ex A exceção InvalidStatusTransitionException.
     * @param request A requisição web.
     * @return Uma ResponseEntity com detalhes do erro.
     */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorDetails> handleInvalidStatusTransitionException(InvalidStatusTransitionException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Lida com a exceção {@link ProjectDeletionException}.
     * Retorna um status HTTP 400 BAD REQUEST.
     * @param ex A exceção ProjectDeletionException.
     * @param request A requisição web.
     * @return Uma ResponseEntity com detalhes do erro.
     */
    @ExceptionHandler(ProjectDeletionException.class)
    public ResponseEntity<ErrorDetails> handleProjectDeletionException(ProjectDeletionException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Lida com a exceção {@link MemberAllocationException}.
     * Retorna um status HTTP 400 BAD REQUEST.
     * @param ex A exceção MemberAllocationException.
     * @param request A requisição web.
     * @return Uma ResponseEntity com detalhes do erro.
     */
    @ExceptionHandler(MemberAllocationException.class)
    public ResponseEntity<ErrorDetails> handleMemberAllocationException(MemberAllocationException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Lida com a exceção {@link ExternalApiException}.
     * Retorna um status HTTP 503 SERVICE UNAVAILABLE.
     * @param ex A exceção ExternalApiException.
     * @param request A requisição web.
     * @return Uma ResponseEntity com detalhes do erro.
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorDetails> handleExternalApiException(ExternalApiException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Lida com exceções de validação de argumentos de método (@Valid).
     * Retorna um status HTTP 400 BAD REQUEST com detalhes dos erros de validação.
     * @param ex A exceção MethodArgumentNotValidException.
     * @param request A requisição web.
     * @return Uma ResponseEntity com detalhes do erro de validação.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), "Erro de validação", request.getDescription(false), errors);
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Lida com exceções genéricas não tratadas especificamente.
     * Retorna um status HTTP 500 INTERNAL SERVER ERROR.
     * @param ex A exceção genérica.
     * @param request A requisição web.
     * @return Uma ResponseEntity com detalhes do erro.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Classe auxiliar para estruturar os detalhes do erro retornados na resposta HTTP.
     */
    public static class ErrorDetails {
        private LocalDateTime timestamp;
        private String message;
        private String details;
        private Map<String, String> validations; // Para erros de validação

        public ErrorDetails(LocalDateTime timestamp, String message, String details) {
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
        }

        public ErrorDetails(LocalDateTime timestamp, String message, String details, Map<String, String> validations) {
            this.timestamp = timestamp;
            this.message = message;
            this.details = details;
            this.validations = validations;
        }

        // Getters para os atributos
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getMessage() {
            return message;
        }

        public String getDetails() {
            return details;
        }

        public Map<String, String> getValidations() {
            return validations;
        }
    }
}
