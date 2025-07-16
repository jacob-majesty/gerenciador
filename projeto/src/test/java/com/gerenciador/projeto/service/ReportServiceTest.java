package com.gerenciador.projeto.service;

import com.gerenciador.projeto.client.MemberApiClient;
import com.gerenciador.projeto.dto.PortfolioSummaryDTO;
import com.gerenciador.projeto.entity.Allocation;
import com.gerenciador.projeto.entity.Project;
import com.gerenciador.projeto.enums.ProjectStatus;
import com.gerenciador.projeto.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ReportService")
class ReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private MemberApiClient memberApiClient; // Mockado, mas não usado diretamente neste teste de resumo

    @InjectMocks
    private ReportService reportService;

    private List<Project> projects;

    @BeforeEach
    void setUp() {
        // Configura projetos de exemplo para os testes
        Project p1 = new Project();
        p1.setId(1L);
        p1.setName("Projeto A");
        p1.setStatus(ProjectStatus.EM_ANDAMENTO);
        p1.setTotalBudget(new BigDecimal("1000.00"));
        p1.setStartDate(LocalDate.of(2023, 1, 1));
        p1.setForecastEndDate(LocalDate.of(2023, 12, 31));
        p1.setAllocations(new HashSet<>(Collections.singletonList(new Allocation(p1, 101L))));

        Project p2 = new Project();
        p2.setId(2L);
        p2.setName("Projeto B");
        p2.setStatus(ProjectStatus.ENCERRADO);
        p2.setTotalBudget(new BigDecimal("2000.00"));
        p2.setStartDate(LocalDate.of(2022, 1, 1));
        p2.setActualEndDate(LocalDate.of(2022, 3, 31)); // Duração de 3 meses (90 dias aprox)
        p2.setAllocations(new HashSet<>(Arrays.asList(new Allocation(p2, 101L), new Allocation(p2, 102L))));

        Project p3 = new Project();
        p3.setId(3L);
        p3.setName("Projeto C");
        p3.setStatus(ProjectStatus.EM_ANALISE);
        p3.setTotalBudget(new BigDecimal("500.00"));
        p3.setStartDate(LocalDate.of(2024, 1, 1));
        p3.setForecastEndDate(LocalDate.of(2024, 6, 30));
        p3.setAllocations(new HashSet<>(Collections.singletonList(new Allocation(p3, 103L))));

        Project p4 = new Project();
        p4.setId(4L);
        p4.setName("Projeto D");
        p4.setStatus(ProjectStatus.ENCERRADO);
        p4.setTotalBudget(new BigDecimal("1500.00"));
        p4.setStartDate(LocalDate.of(2023, 7, 1));
        p4.setActualEndDate(LocalDate.of(2023, 9, 30)); // Duração de 3 meses (90 dias aprox)
        p4.setAllocations(new HashSet<>(Collections.singletonList(new Allocation(p4, 104L))));


        projects = Arrays.asList(p1, p2, p3, p4);
    }

    @Test
    @DisplayName("Deve gerar um resumo de portfólio completo")
    void shouldGenerateCompletePortfolioSummary() {
        when(projectRepository.findAll()).thenReturn(projects);

        PortfolioSummaryDTO summary = reportService.generatePortfolioSummary();

        assertNotNull(summary);

        // Teste: Quantidade de projetos por status
        assertEquals(1L, summary.getProjectsByStatus().get("Em Andamento"));
        assertEquals(2L, summary.getProjectsByStatus().get("Encerrado"));
        assertEquals(1L, summary.getProjectsByStatus().get("Em Análise"));
        assertNull(summary.getProjectsByStatus().get("Cancelado")); // Não há projetos cancelados

        // Teste: Total orçado por status
        assertEquals(new BigDecimal("1000.00"), summary.getTotalBudgetByStatus().get("Em Andamento"));
        assertEquals(new BigDecimal("3500.00"), summary.getTotalBudgetByStatus().get("Encerrado")); // 2000 + 1500
        assertEquals(new BigDecimal("500.00"), summary.getTotalBudgetByStatus().get("Em Análise"));

        // Teste: Média de duração dos projetos encerrados
        // Projeto B: 2022-01-01 a 2022-03-31 = 90 dias
        // Projeto D: 2023-07-01 a 2023-09-30 = 91 dias
        // Média = (90 + 91) / 2 = 90.5
        assertEquals(90.5, summary.getAverageDurationOfFinishedProjects(), 0.01); // Delta para double

        // Teste: Total de membros únicos alocados
        // Membros: 101 (p1, p2), 102 (p2), 103 (p3), 104 (p4) -> 4 membros únicos
        assertEquals(4L, summary.getTotalUniqueMembersAllocated());
    }

    @Test
    @DisplayName("Deve gerar resumo com listas vazias se não houver projetos")
    void shouldGenerateEmptySummaryWhenNoProjects() {
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());

        PortfolioSummaryDTO summary = reportService.generatePortfolioSummary();

        assertNotNull(summary);
        assertTrue(summary.getProjectsByStatus().isEmpty());
        assertTrue(summary.getTotalBudgetByStatus().isEmpty());
        assertEquals(0.0, summary.getAverageDurationOfFinishedProjects());
        assertEquals(0L, summary.getTotalUniqueMembersAllocated());
    }

    @Test
    @DisplayName("Deve calcular média de duração corretamente com um único projeto encerrado")
    void shouldCalculateAverageDurationWithSingleFinishedProject() {
        Project singleFinishedProject = new Project();
        singleFinishedProject.setId(5L);
        singleFinishedProject.setName("Projeto Único Encerrado");
        singleFinishedProject.setStatus(ProjectStatus.ENCERRADO);
        singleFinishedProject.setTotalBudget(new BigDecimal("100.00"));
        singleFinishedProject.setStartDate(LocalDate.of(2023, 1, 1));
        singleFinishedProject.setActualEndDate(LocalDate.of(2023, 1, 10)); // 9 dias de duração
        singleFinishedProject.setAllocations(new HashSet<>(Collections.singletonList(new Allocation(singleFinishedProject, 200L))));

        when(projectRepository.findAll()).thenReturn(Collections.singletonList(singleFinishedProject));

        PortfolioSummaryDTO summary = reportService.generatePortfolioSummary();

        assertEquals(9.0, summary.getAverageDurationOfFinishedProjects());
    }

    @Test
    @DisplayName("Deve lidar com projetos sem datas de término real para média de duração")
    void shouldHandleProjectsWithoutActualEndDateForAverageDuration() {
        Project p1 = new Project();
        p1.setId(1L);
        p1.setStatus(ProjectStatus.ENCERRADO);
        p1.setStartDate(LocalDate.of(2023, 1, 1));
        p1.setActualEndDate(null); // Data real de término nula

        Project p2 = new Project();
        p2.setId(2L);
        p2.setStatus(ProjectStatus.ENCERRADO);
        p2.setStartDate(LocalDate.of(2023, 1, 1));
        p2.setActualEndDate(LocalDate.of(2023, 1, 10)); // 9 dias

        when(projectRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        PortfolioSummaryDTO summary = reportService.generatePortfolioSummary();
        // Apenas p2 deve ser contado, então a média é 9.0
        assertEquals(9.0, summary.getAverageDurationOfFinishedProjects());
    }
}
