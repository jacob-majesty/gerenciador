package com.gerenciador.projeto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando há um problema na alocação de membros.
 * Mapeia para o status HTTP 400 BAD REQUEST.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MemberAllocationException extends RuntimeException {
    public MemberAllocationException(String message) {
        super(message);
    }
}