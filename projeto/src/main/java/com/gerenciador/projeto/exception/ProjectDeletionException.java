package com.gerenciador.projeto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um projeto não pode ser excluído devido a restrições de status.
 * Mapeia para o status HTTP 400 BAD REQUEST.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProjectDeletionException extends RuntimeException {
    public ProjectDeletionException(String message) {
        super(message);
    }
}