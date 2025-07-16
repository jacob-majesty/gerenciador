package com.gerenciador.projeto.service;

import com.gerenciador.projeto.client.MemberApiClient;
import com.gerenciador.projeto.dto.MemberDTO;
import com.gerenciador.projeto.dto.ProjectRequestDTO;
import com.gerenciador.projeto.dto.ProjectResponseDTO;
import com.gerenciador.projeto.dto.ProjectStatusUpdateDTO;
import com.gerenciador.projeto.entity.Allocation;
import com.gerenciador.projeto.entity.Project;
import com.gerenciador.projeto.enums.ProjectStatus;
import com.gerenciador.projeto.enums.RiskLevel;
import com.gerenciador.projeto.exception.*;
import com.gerenciador.projeto.mapper.AllocationMapper;
import com.gerenciador.projeto.mapper.ProjectMapper;
import com.gerenciador.projeto.repository.AllocationRepository;
import com.gerenciador.projeto.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ProjectService")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private AllocationRepository allocationRepository;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private AllocationMapper allocationMapper;
    @Mock
    private MemberApiClient memberApiClient;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private ProjectRequestDTO projectRequestDTO;
    private ProjectResponseDTO projectResponseDTO;
    private MemberDTO managerMemberDTO;
    private MemberDTO employeeMemberDTO;

    @BeforeEach
    void setUp() {
        // Inicializa objetos mock para cada teste
        project = new Project();
        project.setId(1L);
        project.setName("Projeto Teste");
        project.setStartDate(LocalDate.of(2023, 1, 1));
        project.setForecastEndDate(LocalDate.of(2023, 12, 31));
        project.setTotalBudget(new BigDecimal("100000.00"));
        project.setDescription("Descrição do projeto teste.");
        project.setManagerId(10L);
        project.setStatus(ProjectStatus.EM_ANALISE);
        project.setAllocations(new HashSet<>()); // Garante que a coleção não é nula

        projectRequestDTO = new ProjectRequestDTO();
        projectRequestDTO.setName("Novo Projeto");
        projectRequestDTO.setStartDate(LocalDate.of(2024, 1, 1));
        projectRequestDTO.setForecastEndDate(LocalDate.of(2024, 12, 31));
        projectRequestDTO.setTotalBudget(new BigDecimal("200000.00"));
        projectRequestDTO.setManagerId(10L);
        projectRequestDTO.setStatus("EM_ANALISE");

        projectResponseDTO = new ProjectResponseDTO();
        projectResponseDTO.setId(1L);
        projectResponseDTO.setName("Projeto Teste");
        projectResponseDTO.setManagerId(10L);
        projectResponseDTO.setStatus(ProjectStatus.EM_ANALISE);
        projectResponseDTO.setRiskLevel(RiskLevel.BAIXO_RISCO); // Valor padrão para teste

        managerMemberDTO = new MemberDTO(10L, "Gerente Teste", "gerente");
        employeeMemberDTO = new MemberDTO(20L, "Funcionário Teste", "funcionário");
    }

    @Test
    @DisplayName("Deve criar um projeto com sucesso")
    void shouldCreateProjectSuccessfully() {
        // Mock do comportamento
        when(memberApiClient.getMemberById(anyLong())).thenReturn(managerMemberDTO);
        when(projectMapper.toEntity(any(ProjectRequestDTO.class))).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponseDto(any(Project.class))).thenReturn(projectResponseDTO);

        // Executa o método
        ProjectResponseDTO result = projectService.createProject(projectRequestDTO);

        // Verifica as interações e o resultado
        assertNotNull(result);
        assertEquals(projectResponseDTO.getName(), result.getName());
        verify(memberApiClient, times(1)).getMemberById(projectRequestDTO.getManagerId());
        verify(projectRepository, times(1)).save(project);
        verify(projectMapper, times(1)).toEntity(projectRequestDTO);
        verify(projectMapper, times(1)).toResponseDto(project);
    }

    @Test
    @DisplayName("Deve lançar ExternalApiException ao criar projeto com gerente inexistente")
    void shouldThrowExternalApiExceptionWhenCreatingProjectWithNonExistentManager() {
        // Mock para simular gerente não encontrado na API externa
        when(memberApiClient.getMemberById(anyLong())).thenThrow(new RuntimeException("Membro não encontrado"));

        // Verifica se a exceção correta é lançada
        assertThrows(ExternalApiException.class, () -> projectService.createProject(projectRequestDTO));
        verify(memberApiClient, times(1)).getMemberById(projectRequestDTO.getManagerId());
        verify(projectRepository, never()).save(any(Project.class)); // Não deve salvar
    }

    @Test
    @DisplayName("Deve buscar um projeto por ID com sucesso")
    void shouldGetProjectByIdSuccessfully() {
        // Mock do comportamento
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        when(projectMapper.toResponseDto(any(Project.class))).thenReturn(projectResponseDTO);
        when(memberApiClient.getMemberById(project.getManagerId())).thenReturn(managerMemberDTO);

        // Executa o método
        ProjectResponseDTO result = projectService.getProjectById(1L);

        // Verifica
        assertNotNull(result);
        assertEquals(projectResponseDTO.getId(), result.getId());
        assertEquals(managerMemberDTO.getName(), result.getManagerName());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectMapper, times(1)).toResponseDto(project);
        verify(memberApiClient, times(1)).getMemberById(project.getManagerId());
    }

    @Test
    @DisplayName("Deve lançar ProjectNotFoundException ao buscar projeto por ID inexistente")
    void shouldThrowProjectNotFoundExceptionWhenGettingNonExistentProject() {
        // Mock para simular projeto não encontrado
        when(projectRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Verifica
        assertThrows(ProjectNotFoundException.class, () -> projectService.getProjectById(99L));
        verify(projectRepository, times(1)).findById(99L);
        verify(projectMapper, never()).toResponseDto(any(Project.class));
    }

    @Test
    @DisplayName("Deve listar todos os projetos com sucesso")
    void shouldGetAllProjectsSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Collections.singletonList(project), pageable, 1);

        when(projectRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(projectPage);
        when(projectMapper.toResponseDto(any(Project.class))).thenReturn(projectResponseDTO);
        when(memberApiClient.getMemberById(project.getManagerId())).thenReturn(managerMemberDTO);

        Page<ProjectResponseDTO> result = projectService.getAllProjects(null, null, null, null, null, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(projectRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(projectMapper, times(1)).toResponseDto(project);
    }

    @Test
    @DisplayName("Deve atualizar um projeto com sucesso")
    void shouldUpdateProjectSuccessfully() {
        ProjectRequestDTO updateDTO = new ProjectRequestDTO();
        updateDTO.setName("Projeto Atualizado");
        updateDTO.setManagerId(11L); // Novo gerente

        Project updatedProject = new Project();
        updatedProject.setId(1L);
        updatedProject.setName("Projeto Atualizado");
        updatedProject.setManagerId(11L);
        updatedProject.setStatus(ProjectStatus.EM_ANALISE); // Mantém o status original

        ProjectResponseDTO updatedResponseDTO = new ProjectResponseDTO();
        updatedResponseDTO.setId(1L);
        updatedResponseDTO.setName("Projeto Atualizado");
        updatedResponseDTO.setManagerId(11L);
        updatedResponseDTO.setStatus(ProjectStatus.EM_ANALISE);

        MemberDTO newManagerMemberDTO = new MemberDTO(11L, "Novo Gerente", "gerente");


        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        when(memberApiClient.getMemberById(11L)).thenReturn(newManagerMemberDTO); // Mock para o novo gerente
        doNothing().when(projectMapper).updateProjectFromDto(any(ProjectRequestDTO.class), any(Project.class));
        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);
        when(projectMapper.toResponseDto(any(Project.class))).thenReturn(updatedResponseDTO);
        when(memberApiClient.getMemberById(updatedProject.getManagerId())).thenReturn(newManagerMemberDTO); // Mock para o gerente no mapProjectToResponseDTO

        ProjectResponseDTO result = projectService.updateProject(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Projeto Atualizado", result.getName());
        assertEquals(11L, result.getManagerId());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectMapper, times(1)).updateProjectFromDto(updateDTO, project);
        verify(projectRepository, times(1)).save(project);
        verify(memberApiClient, times(1)).getMemberById(11L); // Verifica chamada para o novo gerente
    }

    @Test
    @DisplayName("Deve lançar ProjectNotFoundException ao atualizar projeto inexistente")
    void shouldThrowProjectNotFoundExceptionWhenUpdatingNonExistentProject() {
        when(projectRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ProjectNotFoundException.class, () -> projectService.updateProject(99L, projectRequestDTO));
    }

    @Test
    @DisplayName("Deve lançar InvalidStatusTransitionException ao tentar mudar status via PUT")
    void shouldThrowInvalidStatusTransitionExceptionWhenUpdatingStatusViaPut() {
        ProjectRequestDTO updateDTO = new ProjectRequestDTO();
        updateDTO.setName("Projeto Atualizado");
        updateDTO.setStatus("INICIADO"); // Tentando mudar status via PUT

        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project)); // Project está EM_ANALISE

        assertThrows(InvalidStatusTransitionException.class, () -> projectService.updateProject(1L, updateDTO));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve atualizar o status do projeto com transição válida")
    void shouldUpdateProjectStatusWithValidTransition() {
        ProjectStatusUpdateDTO statusUpdateDTO = new ProjectStatusUpdateDTO();
        statusUpdateDTO.setNewStatus("ANALISE_REALIZADA"); // EM_ANALISE -> ANALISE_REALIZADA é válido

        Project projectInAnalysis = new Project();
        projectInAnalysis.setId(1L);
        projectInAnalysis.setName("Projeto em Análise");
        projectInAnalysis.setManagerId(10L);
        projectInAnalysis.setStatus(ProjectStatus.EM_ANALISE);
        projectInAnalysis.setTotalBudget(new BigDecimal("100000.00"));
        projectInAnalysis.setStartDate(LocalDate.of(2023,1,1));
        projectInAnalysis.setForecastEndDate(LocalDate.of(2023,12,31));

        Project projectAfterUpdate = new Project();
        projectAfterUpdate.setId(1L);
        projectAfterUpdate.setName("Projeto em Análise");
        projectAfterUpdate.setManagerId(10L);
        projectAfterUpdate.setStatus(ProjectStatus.ANALISE_REALIZADA);
        projectAfterUpdate.setTotalBudget(new BigDecimal("100000.00"));
        projectAfterUpdate.setStartDate(LocalDate.of(2023,1,1));
        projectAfterUpdate.setForecastEndDate(LocalDate.of(2023,12,31));

        ProjectResponseDTO responseAfterUpdate = new ProjectResponseDTO();
        responseAfterUpdate.setId(1L);
        responseAfterUpdate.setName("Projeto em Análise");
        responseAfterUpdate.setManagerId(10L);
        responseAfterUpdate.setStatus(ProjectStatus.ANALISE_REALIZADA);
        responseAfterUpdate.setRiskLevel(RiskLevel.BAIXO_RISCO);


        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(projectInAnalysis));
        when(projectRepository.save(any(Project.class))).thenReturn(projectAfterUpdate);
        when(projectMapper.toResponseDto(any(Project.class))).thenReturn(responseAfterUpdate);
        when(memberApiClient.getMemberById(anyLong())).thenReturn(managerMemberDTO); // Para o mapProjectToResponseDTO

        ProjectResponseDTO result = projectService.updateProjectStatus(1L, statusUpdateDTO);

        assertNotNull(result);
        assertEquals(ProjectStatus.ANALISE_REALIZADA, result.getStatus());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(projectInAnalysis); // Verifica que o objeto original foi salvo
    }

    @Test
    @DisplayName("Deve lançar InvalidStatusTransitionException com transição inválida")
    void shouldThrowInvalidStatusTransitionExceptionWithInvalidTransition() {
        ProjectStatusUpdateDTO statusUpdateDTO = new ProjectStatusUpdateDTO();
        statusUpdateDTO.setNewStatus("ENCERRADO"); // EM_ANALISE -> ENCERRADO é inválido

        // project está EM_ANALISE
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));

        assertThrows(InvalidStatusTransitionException.class, () -> projectService.updateProjectStatus(1L, statusUpdateDTO));
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidStatusTransitionException com status inválido (string)")
    void shouldThrowInvalidStatusTransitionExceptionWithInvalidStatusString() {
        ProjectStatusUpdateDTO statusUpdateDTO = new ProjectStatusUpdateDTO();
        statusUpdateDTO.setNewStatus("STATUS_INEXISTENTE");

        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));

        assertThrows(InvalidStatusTransitionException.class, () -> projectService.updateProjectStatus(1L, statusUpdateDTO));
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve definir actualEndDate quando o status é ENCERRADO")
    void shouldSetActualEndDateWhenStatusIsEncerrado() {
        ProjectStatusUpdateDTO statusUpdateDTO = new ProjectStatusUpdateDTO();
        statusUpdateDTO.setNewStatus("ENCERRADO");

        Project projectStarted = new Project();
        projectStarted.setId(1L);
        projectStarted.setName("Projeto Iniciado");
        projectStarted.setManagerId(10L);
        projectStarted.setStatus(ProjectStatus.EM_ANDAMENTO); // Status que permite transição para ENCERRADO
        projectStarted.setStartDate(LocalDate.of(2023,1,1));
        projectStarted.setForecastEndDate(LocalDate.of(2023,12,31));
        projectStarted.setTotalBudget(new BigDecimal("100000.00"));


        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(projectStarted));
        when(projectRepository.save(any(Project.class))).thenReturn(projectStarted); // Retorna o mesmo objeto para verificar
        when(projectMapper.toResponseDto(any(Project.class))).thenReturn(projectResponseDTO); // Mock genérico
        when(memberApiClient.getMemberById(anyLong())).thenReturn(managerMemberDTO); // Para o mapProjectToResponseDTO

        projectService.updateProjectStatus(1L, statusUpdateDTO);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();

        assertEquals(ProjectStatus.ENCERRADO, savedProject.getStatus());
        assertNotNull(savedProject.getActualEndDate());
        assertEquals(LocalDate.now(), savedProject.getActualEndDate());
    }

    @Test
    @DisplayName("Deve excluir um projeto com sucesso (status permitido)")
    void shouldDeleteProjectSuccessfullyWithAllowedStatus() {
        project.setStatus(ProjectStatus.EM_ANALISE); // Status permitido para exclusão
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        doNothing().when(projectRepository).delete(any(Project.class));

        assertDoesNotThrow(() -> projectService.deleteProject(1L));
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).delete(project);
    }

    @Test
    @DisplayName("Deve lançar ProjectDeletionException ao excluir projeto com status proibido")
    void shouldThrowProjectDeletionExceptionWhenDeletingProjectWithForbiddenStatus() {
        project.setStatus(ProjectStatus.EM_ANDAMENTO); // Status proibido para exclusão
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));

        assertThrows(ProjectDeletionException.class, () -> projectService.deleteProject(1L));
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("Deve alocar membros com sucesso")
    void shouldAllocateMembersSuccessfully() {
        List<Long> memberIds = Arrays.asList(20L, 21L); // IDs de funcionários
        MemberDTO employee1 = new MemberDTO(20L, "Func1", "funcionário");
        MemberDTO employee2 = new MemberDTO(21L, "Func2", "funcionário");

        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        when(memberApiClient.getMemberById(20L)).thenReturn(employee1);
        when(memberApiClient.getMemberById(21L)).thenReturn(employee2);
        when(allocationRepository.existsByProjectIdAndMemberId(anyLong(), anyLong())).thenReturn(false); // Não existem alocações
        when(projectRepository.findProjectsByAllocatedMemberAndStatusNotIn(anyLong(), anyList())).thenReturn(Collections.emptyList()); // Nenhum projeto ativo
        when(projectRepository.save(any(Project.class))).thenReturn(project); // Retorna o mesmo projeto
        when(projectMapper.toResponseDto(any(Project.class))).thenReturn(projectResponseDTO); // Mock genérico
        when(memberApiClient.getMemberById(project.getManagerId())).thenReturn(managerMemberDTO); // Para o mapProjectToResponseDTO

        ProjectResponseDTO result = projectService.allocateMembersToProject(1L, memberIds);

        assertNotNull(result);
        verify(projectRepository, times(1)).findById(1L);
        verify(memberApiClient, times(2)).getMemberById(anyLong()); // Para cada membro
        verify(allocationRepository, times(2)).existsByProjectIdAndMemberId(anyLong(), anyLong());
        verify(projectRepository, times(2)).findProjectsByAllocatedMemberAndStatusNotIn(anyLong(), anyList());
        verify(projectRepository, times(1)).save(project);

        // Verifica se as alocações foram adicionadas ao projeto
        assertEquals(2, project.getAllocations().size());
    }

    @Test
    @DisplayName("Deve lançar MemberAllocationException se membro não for funcionário")
    void shouldThrowMemberAllocationExceptionIfMemberIsNotEmployee() {
        List<Long> memberIds = Collections.singletonList(30L);
        MemberDTO nonEmployee = new MemberDTO(30L, "Terceiro", "terceiro");

        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        when(memberApiClient.getMemberById(30L)).thenReturn(nonEmployee);

        assertThrows(MemberAllocationException.class, () -> projectService.allocateMembersToProject(1L, memberIds));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve lançar MemberAllocationException se membro já estiver alocado")
    void shouldThrowMemberAllocationExceptionIfMemberAlreadyAllocated() {
        List<Long> memberIds = Collections.singletonList(20L);
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        when(memberApiClient.getMemberById(20L)).thenReturn(employeeMemberDTO);
        when(allocationRepository.existsByProjectIdAndMemberId(anyLong(), anyLong())).thenReturn(true); // Já alocado

        assertThrows(MemberAllocationException.class, () -> projectService.allocateMembersToProject(1L, memberIds));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve lançar MemberAllocationException se membro exceder limite de projetos")
    void shouldThrowMemberAllocationExceptionIfMemberExceedsProjectLimit() {
        List<Long> memberIds = Collections.singletonList(20L);
        Project activeProject1 = new Project(); activeProject1.setId(10L); activeProject1.setStatus(ProjectStatus.EM_ANDAMENTO);
        Project activeProject2 = new Project(); activeProject2.setId(11L); activeProject2.setStatus(ProjectStatus.INICIADO);
        Project activeProject3 = new Project(); activeProject3.setId(12L); activeProject3.setStatus(ProjectStatus.PLANEJADO);

        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        when(memberApiClient.getMemberById(20L)).thenReturn(employeeMemberDTO);
        when(allocationRepository.existsByProjectIdAndMemberId(anyLong(), anyLong())).thenReturn(false);
        when(projectRepository.findProjectsByAllocatedMemberAndStatusNotIn(anyLong(), anyList()))
                .thenReturn(Arrays.asList(activeProject1, activeProject2, activeProject3)); // 3 projetos ativos

        assertThrows(MemberAllocationException.class, () -> projectService.allocateMembersToProject(1L, memberIds));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve lançar MemberAllocationException se a lista de membros for vazia")
    void shouldThrowMemberAllocationExceptionIfMemberListIsEmpty() {
        assertThrows(MemberAllocationException.class, () -> projectService.allocateMembersToProject(1L, Collections.emptyList()));
        verify(projectRepository, never()).findById(anyLong()); // Nem tenta buscar o projeto
    }

    @Test
    @DisplayName("Deve lançar MemberAllocationException se a lista de membros exceder o máximo (10)")
    void shouldThrowMemberAllocationExceptionIfMemberListExceedsMax() {
        List<Long> memberIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L); // 11 membros
        assertThrows(MemberAllocationException.class, () -> projectService.allocateMembersToProject(1L, memberIds));
        verify(projectRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Deve desalocar um membro com sucesso")
    void shouldDeallocateMemberSuccessfully() {
        Allocation allocation = new Allocation(project, 20L);
        project.addAllocation(allocation); // Adiciona a alocação ao projeto mock

        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project); // Retorna o mesmo projeto

        assertDoesNotThrow(() -> projectService.deallocateMemberFromProject(1L, 20L));
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(project);
        assertTrue(project.getAllocations().isEmpty()); // Verifica se a alocação foi removida
    }

    @Test
    @DisplayName("Deve lançar MemberAllocationException se membro não estiver alocado")
    void shouldThrowMemberAllocationExceptionIfMemberNotAllocated() {
        // Projeto sem alocações
        when(projectRepository.findById(anyLong())).thenReturn(Optional.of(project));

        assertThrows(MemberAllocationException.class, () -> projectService.deallocateMemberFromProject(1L, 99L));
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("Deve calcular risco BAIXO_RISCO")
    void shouldCalculateLowRisk() {
        BigDecimal budget = new BigDecimal("50000.00");
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate forecastEndDate = LocalDate.of(2023, 3, 31); // 3 meses

        RiskLevel risk = projectService.calculateRiskLevel(budget, startDate, forecastEndDate);
        assertEquals(RiskLevel.BAIXO_RISCO, risk);
    }

    @Test
    @DisplayName("Deve calcular risco MEDIO_RISCO (orçamento)")
    void shouldCalculateMediumRiskByBudget() {
        BigDecimal budget = new BigDecimal("200000.00");
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate forecastEndDate = LocalDate.of(2023, 2, 28); // 1 mês

        RiskLevel risk = projectService.calculateRiskLevel(budget, startDate, forecastEndDate);
        assertEquals(RiskLevel.MEDIO_RISCO, risk);
    }

    @Test
    @DisplayName("Deve calcular risco MEDIO_RISCO (duração)")
    void shouldCalculateMediumRiskByDuration() {
        BigDecimal budget = new BigDecimal("50000.00");
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate forecastEndDate = LocalDate.of(2023, 6, 30); // 5 meses

        RiskLevel risk = projectService.calculateRiskLevel(budget, startDate, forecastEndDate);
        assertEquals(RiskLevel.MEDIO_RISCO, risk);
    }

    @Test
    @DisplayName("Deve calcular risco ALTO_RISCO (orçamento)")
    void shouldCalculateHighRiskByBudget() {
        BigDecimal budget = new BigDecimal("600000.00");
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate forecastEndDate = LocalDate.of(2023, 2, 28); // 1 mês

        RiskLevel risk = projectService.calculateRiskLevel(budget, startDate, forecastEndDate);
        assertEquals(RiskLevel.ALTO_RISCO, risk);
    }

    @Test
    @DisplayName("Deve calcular risco ALTO_RISCO (duração)")
    void shouldCalculateHighRiskByDuration() {
        BigDecimal budget = new BigDecimal("50000.00");
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate forecastEndDate = LocalDate.of(2024, 1, 1); // 12 meses

        RiskLevel risk = projectService.calculateRiskLevel(budget, startDate, forecastEndDate);
        assertEquals(RiskLevel.ALTO_RISCO, risk);
    }

    @Test
    @DisplayName("Deve retornar BAIXO_RISCO se datas forem nulas")
    void shouldReturnLowRiskIfDatesAreNull() {
        BigDecimal budget = new BigDecimal("100000.00");
        RiskLevel risk = projectService.calculateRiskLevel(budget, null, null);
        assertEquals(RiskLevel.BAIXO_RISCO, risk);
    }
}
