package com.gerenciador.projeto.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para receber dados na criação e atualização de um projeto.
 * Contém validações básicas para os campos de entrada.
 */
public class ProjectRequestDTO {

    @NotBlank(message = "O nome do projeto é obrigatório.")
    private String name;

    @NotNull(message = "A data de início é obrigatória.")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;

    @NotNull(message = "A previsão de término é obrigatória.")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate forecastEndDate;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate actualEndDate; // Pode ser nulo se não finalizado

    @NotNull(message = "O orçamento total é obrigatório.")
    @DecimalMin(value = "0.0", inclusive = true, message = "O orçamento deve ser maior ou igual a zero.")
    private BigDecimal totalBudget;

    private String description;

    @NotNull(message = "O gerente do projeto é obrigatório.")
    private Long managerId; // Relacionamento com membro via API externa
    @NotNull(message = "O status do projeto é obrigatório.")
    private String status; // O status será validado como enum no serviço

    // Getters e Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getForecastEndDate() {
        return forecastEndDate;
    }

    public void setForecastEndDate(LocalDate forecastEndDate) {
        this.forecastEndDate = forecastEndDate;
    }

    public LocalDate getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}