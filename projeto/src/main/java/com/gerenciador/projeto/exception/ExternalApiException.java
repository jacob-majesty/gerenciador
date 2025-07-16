package com.gerenciador.projeto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando ocorre um erro na comunicação com uma API externa (ex: API de membros).
 * Mapeia para o status HTTP 503 SERVICE UNAVAILABLE.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
    public ExternalApiException(String message) {
        super(message);
    }
}
