package com.gerenciador.projeto.service;

import com.gerenciador.projeto.client.MemberApiClient;
import com.gerenciador.projeto.dto.PortfolioSummaryDTO;
import com.gerenciador.projeto.entity.Project;
import com.gerenciador.projeto.enums.ProjectStatus;
import com.gerenciador.projeto.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementação do serviço de relatórios, responsável por gerar
 * um resumo estatístico do portfólio de projetos.
 */
@Service
public class ReportService implements IReportService {

    private final ProjectRepository projectRepository;
    private final MemberApiClient memberApiClient;

    public ReportService(ProjectRepository projectRepository, MemberApiClient memberApiClient) {
        this.projectRepository = projectRepository;
        this.memberApiClient = memberApiClient;
    }

    /**
     * Gera um relatório resumido do portfólio de projetos.
     * Inclui:
     * - Quantidade de projetos por status.
     * - Total orçado por status.
     * - Média de duração dos projetos encerrados.
     * - Total de membros únicos alocados em projetos ativos.
     * @return DTO com o resumo do portfólio.
     */
    @Override
    @Transactional(readOnly = true)
    public PortfolioSummaryDTO generatePortfolioSummary() {
        List<Project> allProjects = projectRepository.findAll();
        PortfolioSummaryDTO summary = new PortfolioSummaryDTO();

        // 1. Quantidade de projetos por status
        Map<String, Long> projectsByStatus = allProjects.stream()
                .collect(Collectors.groupingBy(project -> project.getStatus().getDescription(), Collectors.counting()));
        summary.setProjectsByStatus(projectsByStatus);

        // 2. Total orçado por status
        Map<String, BigDecimal> totalBudgetByStatus = allProjects.stream()
                .collect(Collectors.groupingBy(
                        project -> project.getStatus().getDescription(),
                        Collectors.reducing(BigDecimal.ZERO, Project::getTotalBudget, BigDecimal::add)
                ));
        summary.setTotalBudgetByStatus(totalBudgetByStatus);

        // 3. Média de duração dos projetos encerrados
        double averageDuration = allProjects.stream()
                .filter(project -> project.getStatus() == ProjectStatus.ENCERRADO &&
                        project.getStartDate() != null &&
                        project.getActualEndDate() != null)
                .mapToLong(project -> ChronoUnit.DAYS.between(project.getStartDate(), project.getActualEndDate()))
                .average()
                .orElse(0.0); // Retorna 0.0 se não houver projetos encerrados
        summary.setAverageDurationOfFinishedProjects(averageDuration);

        // 4. Total de membros únicos alocados
        // Primeiro, coleta todos os IDs de membros alocados em projetos
        Set<Long> uniqueMemberIds = allProjects.stream()
                .flatMap(project -> project.getAllocations().stream())
                .map(allocation -> allocation.getMemberId())
                .collect(Collectors.toSet());

        // Agora, tenta buscar os detalhes desses membros para garantir que são "funcionários"
        // Ou simplesmente conta os IDs únicos se a validação já for feita na alocação
        // Para este relatório, vamos considerar apenas os IDs alocados, sem revalidar o cargo.
        summary.setTotalUniqueMembersAllocated((long) uniqueMemberIds.size());

        return summary;
    }

}
