package com.gerenciador.projeto.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO para o relatório resumido do portfólio.
 * Contém estatísticas agregadas sobre os projetos.
 */
public class PortfolioSummaryDTO {

    private Map<String, Long> projectsByStatus; // Quantidade de projetos por status
    private Map<String, BigDecimal> totalBudgetByStatus; // Total orçado por status
    private Double averageDurationOfFinishedProjects; // Média de duração dos projetos encerrados
    private Long totalUniqueMembersAllocated; // Total de membros únicos alocados

    // Getters e Setters
    public Map<String, Long> getProjectsByStatus() {
        return projectsByStatus;
    }

    public void setProjectsByStatus(Map<String, Long> projectsByStatus) {
        this.projectsByStatus = projectsByStatus;
    }

    public Map<String, BigDecimal> getTotalBudgetByStatus() {
        return totalBudgetByStatus;
    }

    public void setTotalBudgetByStatus(Map<String, BigDecimal> totalBudgetByStatus) {
        this.totalBudgetByStatus = totalBudgetByStatus;
    }

    public Double getAverageDurationOfFinishedProjects() {
        return averageDurationOfFinishedProjects;
    }

    public void setAverageDurationOfFinishedProjects(Double averageDurationOfFinishedProjects) {
        this.averageDurationOfFinishedProjects = averageDurationOfFinishedProjects;
    }

    public Long getTotalUniqueMembersAllocated() {
        return totalUniqueMembersAllocated;
    }

    public void setTotalUniqueMembersAllocated(Long totalUniqueMembersAllocated) {
        this.totalUniqueMembersAllocated = totalUniqueMembersAllocated;
    }

}
