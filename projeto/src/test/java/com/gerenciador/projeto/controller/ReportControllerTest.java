package com.gerenciador.projeto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gerenciador.projeto.dto.PortfolioSummaryDTO;
import com.gerenciador.projeto.service.IReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class) // Testa apenas a camada web para ReportController
@DisplayName("Testes para ReportController")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IReportService reportService;

    private ObjectMapper objectMapper;
    private PortfolioSummaryDTO portfolioSummaryDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        portfolioSummaryDTO = new PortfolioSummaryDTO();
        Map<String, Long> projectsByStatus = new HashMap<>();
        projectsByStatus.put("Em Andamento", 5L);
        projectsByStatus.put("Encerrado", 3L);
        portfolioSummaryDTO.setProjectsByStatus(projectsByStatus);

        Map<String, BigDecimal> totalBudgetByStatus = new HashMap<>();
        totalBudgetByStatus.put("Em Andamento", new BigDecimal("500000.00"));
        totalBudgetByStatus.put("Encerrado", new BigDecimal("300000.00"));
        portfolioSummaryDTO.setTotalBudgetByStatus(totalBudgetByStatus);

        portfolioSummaryDTO.setAverageDurationOfFinishedProjects(120.5);
        portfolioSummaryDTO.setTotalUniqueMembersAllocated(15L);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve gerar resumo do portfólio e retornar status 200 OK")
    void shouldGeneratePortfolioSummaryAndReturn200() throws Exception {
        when(reportService.generatePortfolioSummary()).thenReturn(portfolioSummaryDTO);

        mockMvc.perform(get("/api/relatorios/resumo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectsByStatus.['Em Andamento']").value(5L))
                .andExpect(jsonPath("$.totalUniqueMembersAllocated").value(15L));

        verify(reportService, times(1)).generatePortfolioSummary();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve retornar 500 INTERNAL SERVER ERROR se o serviço lançar exceção")
    void shouldReturn500IfServiceThrowsException() throws Exception {
        when(reportService.generatePortfolioSummary()).thenThrow(new RuntimeException("Erro interno de serviço"));

        mockMvc.perform(get("/api/relatorios/resumo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro interno de serviço"));

        verify(reportService, times(1)).generatePortfolioSummary();
    }
}
