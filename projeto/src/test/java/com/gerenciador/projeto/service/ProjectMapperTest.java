package com.gerenciador.projeto.service;

import com.gerenciador.projeto.dto.ProjectRequestDTO;
import com.gerenciador.projeto.dto.ProjectResponseDTO;
import com.gerenciador.projeto.entity.Project;
import com.gerenciador.projeto.enums.ProjectStatus;
import com.gerenciador.projeto.enums.RiskLevel;
import com.gerenciador.projeto.mapper.ProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes para ProjectMapper")
class ProjectMapperTest {

    private ProjectMapper projectMapper;

    @BeforeEach
    void setUp() {
        projectMapper = new ProjectMapper();
    }

    @Test
    @DisplayName("Deve mapear ProjectRequestDTO para Project Entity corretamente")
    void shouldMapRequestDtoToEntityCorrectly() {
        ProjectRequestDTO dto = new ProjectRequestDTO();
        dto.setName("Projeto de Mapeamento");
        dto.setStartDate(LocalDate.of(2023, 1, 1));
        dto.setForecastEndDate(LocalDate.of(2023, 12, 31));
        dto.setActualEndDate(null);
        dto.setTotalBudget(new BigDecimal("150000.00"));
        dto.setDescription("Descrição do projeto.");
        dto.setManagerId(10L);
        dto.setStatus("EM_ANDAMENTO");

        Project project = projectMapper.toEntity(dto);

        assertNotNull(project);
        assertEquals(dto.getName(), project.getName());
        assertEquals(dto.getStartDate(), project.getStartDate());
        assertEquals(dto.getForecastEndDate(), project.getForecastEndDate());
        assertNull(project.getActualEndDate());
        assertEquals(dto.getTotalBudget(), project.getTotalBudget());
        assertEquals(dto.getDescription(), project.getDescription());
        assertEquals(dto.getManagerId(), project.getManagerId());
        assertEquals(ProjectStatus.EM_ANDAMENTO, project.getStatus());
        assertTrue(project.getAllocations().isEmpty()); // Deve ser inicializado vazio
    }

    @Test
    @DisplayName("Deve mapear Project Entity para ProjectResponseDTO corretamente")
    void shouldMapEntityToResponseDtoCorrectly() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Projeto Resposta");
        project.setStartDate(LocalDate.of(2023, 1, 1));
        project.setForecastEndDate(LocalDate.of(2023, 3, 31)); // 3 meses
        project.setActualEndDate(null);
        project.setTotalBudget(new BigDecimal("80000.00"));
        project.setDescription("Descrição da resposta.");
        project.setManagerId(10L);
        project.setStatus(ProjectStatus.INICIADO);

        ProjectResponseDTO dto = projectMapper.toResponseDto(project);

        assertNotNull(dto);
        assertEquals(project.getId(), dto.getId());
        assertEquals(project.getName(), dto.getName());
        assertEquals(project.getStartDate(), dto.getStartDate());
        assertEquals(project.getForecastEndDate(), dto.getForecastEndDate());
        assertNull(dto.getActualEndDate());
        assertEquals(project.getTotalBudget(), dto.getTotalBudget());
        assertEquals(project.getDescription(), dto.getDescription());
        assertEquals(project.getManagerId(), dto.getManagerId());
        assertEquals(project.getStatus(), dto.getStatus());
        // O RiskLevel é calculado no Service, então aqui o mapper apenas o define se já estiver na entidade
        // Mas como o mapper original tinha a lógica, vamos garantir que ele não quebre
        assertNotNull(dto.getRiskLevel()); // O mapper ainda tem a lógica de cálculo
        assertEquals(RiskLevel.BAIXO_RISCO, dto.getRiskLevel()); // Baseado nos dados de exemplo
    }

    @Test
    @DisplayName("Deve atualizar Project Entity de ProjectRequestDTO corretamente")
    void shouldUpdateProjectFromDtoCorrectly() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Nome Antigo");
        project.setStartDate(LocalDate.of(2022, 1, 1));
        project.setForecastEndDate(LocalDate.of(2022, 12, 31));
        project.setActualEndDate(null);
        project.setTotalBudget(new BigDecimal("50000.00"));
        project.setDescription("Descrição Antiga");
        project.setManagerId(1L);
        project.setStatus(ProjectStatus.EM_ANALISE);

        ProjectRequestDTO updateDto = new ProjectRequestDTO();
        updateDto.setName("Nome Novo");
        updateDto.setStartDate(LocalDate.of(2023, 1, 1));
        updateDto.setForecastEndDate(LocalDate.of(2023, 6, 30));
        updateDto.setActualEndDate(LocalDate.of(2023, 6, 30));
        updateDto.setTotalBudget(new BigDecimal("75000.00"));
        updateDto.setDescription("Descrição Nova");
        updateDto.setManagerId(2L);
        updateDto.setStatus("INICIADO"); // Este campo deve ser ignorado pelo mapper, pois é atualizado via PATCH

        projectMapper.updateProjectFromDto(updateDto, project);

        assertEquals("Nome Novo", project.getName());
        assertEquals(LocalDate.of(2023, 1, 1), project.getStartDate());
        assertEquals(LocalDate.of(2023, 6, 30), project.getForecastEndDate());
        assertEquals(LocalDate.of(2023, 6, 30), project.getActualEndDate());
        assertEquals(new BigDecimal("75000.00"), project.getTotalBudget());
        assertEquals("Descrição Nova", project.getDescription());
        assertEquals(2L, project.getManagerId());
        // O status NÃO deve ser alterado por este método, pois é responsabilidade do PATCH /status
        assertEquals(ProjectStatus.EM_ANALISE, project.getStatus());
    }

    @Test
    @DisplayName("Deve retornar null ao mapear DTO nulo para entidade")
    void shouldReturnNullWhenMappingNullDtoToEntity() {
        assertNull(projectMapper.toEntity(null));
    }

    @Test
    @DisplayName("Deve retornar null ao mapear entidade nula para DTO")
    void shouldReturnNullWhenMappingNullEntityToDto() {
        assertNull(projectMapper.toResponseDto(null));
    }
}