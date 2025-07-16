package com.gerenciador.projeto.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gerenciador.projeto.enums.ProjectStatus;
import com.gerenciador.projeto.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para enviar dados de um projeto como resposta ao cliente.
 * Inclui campos calculados dinamicamente, como o nível de risco.
 */
public class ProjectResponseDTO {

    private Long id;
    private String name;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate startDate;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate forecastEndDate;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate actualEndDate;

    private BigDecimal totalBudget;
    private String description;
    private Long managerId;
    private String managerName; // Nome do gerente para exibição (obtido da API externa)
    private ProjectStatus status;
    private RiskLevel riskLevel; //

    private List<MemberAllocationDTO> allocatedMembers; // Membros alocados no projeto

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<MemberAllocationDTO> getAllocatedMembers() {
        return allocatedMembers;
    }

    public void setAllocatedMembers(List<MemberAllocationDTO> allocatedMembers) {
        this.allocatedMembers = allocatedMembers;
    }
}