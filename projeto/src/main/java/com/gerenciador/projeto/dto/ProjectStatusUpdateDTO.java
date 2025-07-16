package com.gerenciador.projeto.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para receber o novo status de um projeto em uma requisição PATCH.
 */
public class ProjectStatusUpdateDTO {

    @NotBlank(message = "O novo status é obrigatório.")
    private String newStatus;

    // Getters e Setters
    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}
