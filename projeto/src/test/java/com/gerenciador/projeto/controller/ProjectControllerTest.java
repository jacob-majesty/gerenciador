package com.gerenciador.projeto.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gerenciador.projeto.dto.MemberAllocationDTO;
import com.gerenciador.projeto.dto.ProjectRequestDTO;
import com.gerenciador.projeto.dto.ProjectResponseDTO;
import com.gerenciador.projeto.dto.ProjectStatusUpdateDTO;
import com.gerenciador.projeto.enums.ProjectStatus;
import com.gerenciador.projeto.enums.RiskLevel;
import com.gerenciador.projeto.exception.InvalidStatusTransitionException;
import com.gerenciador.projeto.exception.MemberAllocationException;
import com.gerenciador.projeto.exception.ProjectDeletionException;
import com.gerenciador.projeto.exception.ProjectNotFoundException;
import com.gerenciador.projeto.service.IProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class) // Testa apenas a camada web para ProjectController
@DisplayName("Testes para ProjectController")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc; // Objeto para simular requisições HTTP

    @MockBean // Cria um mock do IProjectService e injeta no controller
    private IProjectService projectService;

    private ObjectMapper objectMapper; // Para converter objetos Java em JSON e vice-versa

    private ProjectRequestDTO projectRequestDTO;
    private ProjectResponseDTO projectResponseDTO;
    private ProjectStatusUpdateDTO statusUpdateDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Suporte para LocalDate

        projectRequestDTO = new ProjectRequestDTO();
        projectRequestDTO.setName("Novo Projeto Teste");
        projectRequestDTO.setStartDate(LocalDate.of(2024, 1, 1));
        projectRequestDTO.setForecastEndDate(LocalDate.of(2024, 12, 31));
        projectRequestDTO.setTotalBudget(new BigDecimal("100000.00"));
        projectRequestDTO.setManagerId(1L);
        projectRequestDTO.setStatus("EM_ANALISE");

        projectResponseDTO = new ProjectResponseDTO();
        projectResponseDTO.setId(1L);
        projectResponseDTO.setName("Novo Projeto Teste");
        projectResponseDTO.setStartDate(LocalDate.of(2024, 1, 1));
        projectResponseDTO.setForecastEndDate(LocalDate.of(2024, 12, 31));
        projectResponseDTO.setTotalBudget(new BigDecimal("100000.00"));
        projectResponseDTO.setManagerId(1L);
        projectResponseDTO.setManagerName("Gerente Teste");
        projectResponseDTO.setStatus(ProjectStatus.EM_ANALISE);
        projectResponseDTO.setRiskLevel(RiskLevel.BAIXO_RISCO);
        projectResponseDTO.setAllocatedMembers(Collections.emptyList());

        statusUpdateDTO = new ProjectStatusUpdateDTO();
        statusUpdateDTO.setNewStatus("ANALISE_REALIZADA");
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"}) // Simula um usuário autenticado
    @DisplayName("Deve criar um projeto e retornar status 201 CREATED")
    void shouldCreateProjectAndReturn201() throws Exception {
        when(projectService.createProject(any(ProjectRequestDTO.class))).thenReturn(projectResponseDTO);

        mockMvc.perform(post("/api/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Novo Projeto Teste"));

        verify(projectService, times(1)).createProject(any(ProjectRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve retornar 400 BAD REQUEST ao criar projeto com DTO inválido")
    void shouldReturn400WhenCreatingProjectWithInvalidDto() throws Exception {
        projectRequestDTO.setName(""); // Nome vazio, o que é inválido

        mockMvc.perform(post("/api/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validação"));

        verify(projectService, never()).createProject(any(ProjectRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve buscar um projeto por ID e retornar status 200 OK")
    void shouldGetProjectByIdAndReturn200() throws Exception {
        when(projectService.getProjectById(anyLong())).thenReturn(projectResponseDTO);

        mockMvc.perform(get("/api/projetos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Novo Projeto Teste"));

        verify(projectService, times(1)).getProjectById(1L);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve retornar 404 NOT FOUND ao buscar projeto inexistente")
    void shouldReturn404WhenGettingNonExistentProject() throws Exception {
        when(projectService.getProjectById(anyLong())).thenThrow(new ProjectNotFoundException("Projeto não encontrado"));

        mockMvc.perform(get("/api/projetos/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Projeto não encontrado"));

        verify(projectService, times(1)).getProjectById(99L);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve listar todos os projetos e retornar status 200 OK")
    void shouldGetAllProjectsAndReturn200() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<ProjectResponseDTO> page = new PageImpl<>(Collections.singletonList(projectResponseDTO), pageable, 1);
        when(projectService.getAllProjects(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/projetos")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));

        verify(projectService, times(1)).getAllProjects(null, null, null, null, null, pageable);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve atualizar um projeto e retornar status 200 OK")
    void shouldUpdateProjectAndReturn200() throws Exception {
        projectRequestDTO.setName("Projeto Atualizado");
        projectResponseDTO.setName("Projeto Atualizado"); // Atualiza o mock de resposta

        when(projectService.updateProject(anyLong(), any(ProjectRequestDTO.class))).thenReturn(projectResponseDTO);

        mockMvc.perform(put("/api/projetos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Projeto Atualizado"));

        verify(projectService, times(1)).updateProject(eq(1L), any(ProjectRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve retornar 404 NOT FOUND ao atualizar projeto inexistente")
    void shouldReturn404WhenUpdatingNonExistentProject() throws Exception {
        when(projectService.updateProject(anyLong(), any(ProjectRequestDTO.class)))
                .thenThrow(new ProjectNotFoundException("Projeto não encontrado"));

        mockMvc.perform(put("/api/projetos/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Projeto não encontrado"));

        verify(projectService, times(1)).updateProject(eq(99L), any(ProjectRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve atualizar o status do projeto e retornar status 200 OK")
    void shouldUpdateProjectStatusAndReturn200() throws Exception {
        projectResponseDTO.setStatus(ProjectStatus.ANALISE_REALIZADA); // Atualiza o mock de resposta

        when(projectService.updateProjectStatus(anyLong(), any(ProjectStatusUpdateDTO.class)))
                .thenReturn(projectResponseDTO);

        mockMvc.perform(patch("/api/projetos/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANALISE_REALIZADA"));

        verify(projectService, times(1)).updateProjectStatus(eq(1L), any(ProjectStatusUpdateDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve retornar 400 BAD REQUEST ao atualizar status com transição inválida")
    void shouldReturn400WhenUpdatingStatusWithInvalidTransition() throws Exception {
        when(projectService.updateProjectStatus(anyLong(), any(ProjectStatusUpdateDTO.class)))
                .thenThrow(new InvalidStatusTransitionException("Transição inválida"));

        mockMvc.perform(patch("/api/projetos/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transição inválida"));

        verify(projectService, times(1)).updateProjectStatus(eq(1L), any(ProjectStatusUpdateDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve excluir um projeto e retornar status 204 NO CONTENT")
    void shouldDeleteProjectAndReturn204() throws Exception {
        doNothing().when(projectService).deleteProject(anyLong());

        mockMvc.perform(delete("/api/projetos/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProject(1L);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve retornar 400 BAD REQUEST ao tentar excluir projeto com status proibido")
    void shouldReturn400WhenDeletingProjectWithForbiddenStatus() throws Exception {
        doThrow(new ProjectDeletionException("Não é possível excluir")).when(projectService).deleteProject(anyLong());

        mockMvc.perform(delete("/api/projetos/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Não é possível excluir"));

        verify(projectService, times(1)).deleteProject(1L);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve alocar membros e retornar status 200 OK")
    void shouldAllocateMembersAndReturn200() throws Exception {
        List<Long> memberIds = Arrays.asList(101L, 102L);
        when(projectService.allocateMembersToProject(anyLong(), anyList())).thenReturn(projectResponseDTO);

        mockMvc.perform(post("/api/projetos/{id}/membros", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(projectService, times(1)).allocateMembersToProject(eq(1L), eq(memberIds));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve retornar 400 BAD REQUEST ao tentar alocar membros com erro de alocação")
    void shouldReturn400WhenAllocatingMembersWithError() throws Exception {
        List<Long> memberIds = Collections.singletonList(101L);
        when(projectService.allocateMembersToProject(anyLong(), anyList()))
                .thenThrow(new MemberAllocationException("Membro já alocado"));

        mockMvc.perform(post("/api/projetos/{id}/membros", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberIds)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Membro já alocado"));

        verify(projectService, times(1)).allocateMembersToProject(eq(1L), eq(memberIds));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve desalocar um membro e retornar status 204 NO CONTENT")
    void shouldDeallocateMemberAndReturn204() throws Exception {
        doNothing().when(projectService).deallocateMemberFromProject(anyLong(), anyLong());

        mockMvc.perform(delete("/api/projetos/{projectId}/membros/{memberId}", 1L, 101L))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deallocateMemberFromProject(1L, 101L);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve retornar 400 BAD REQUEST ao tentar desalocar membro não alocado")
    void shouldReturn400WhenDeallocatingNonAllocatedMember() throws Exception {
        doThrow(new MemberAllocationException("Membro não alocado")).when(projectService).deallocateMemberFromProject(anyLong(), anyLong());

        mockMvc.perform(delete("/api/projetos/{projectId}/membros/{memberId}", 1L, 999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Membro não alocado"));

        verify(projectService, times(1)).deallocateMemberFromProject(1L, 999L);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve listar membros alocados e retornar status 200 OK")
    void shouldListAllocatedMembersAndReturn200() throws Exception {
        MemberAllocationDTO member1 = new MemberAllocationDTO();
        member1.setMemberId(101L);
        member1.setMemberName("Funcionario Um");
        List<MemberAllocationDTO> allocatedMembers = Collections.singletonList(member1);

        when(projectService.getAllocatedMembers(anyLong())).thenReturn(allocatedMembers);

        mockMvc.perform(get("/api/projetos/{id}/membros", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(101L))
                .andExpect(jsonPath("$[0].memberName").value("Funcionario Um"));

        verify(projectService, times(1)).getAllocatedMembers(1L);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Deve retornar 404 NOT FOUND ao listar membros de projeto inexistente")
    void shouldReturn404WhenListingMembersOfNonExistentProject() throws Exception {
        when(projectService.getAllocatedMembers(anyLong()))
                .thenThrow(new ProjectNotFoundException("Projeto não encontrado"));

        mockMvc.perform(get("/api/projetos/{id}/membros", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Projeto não encontrado"));

        verify(projectService, times(1)).getAllocatedMembers(99L);
    }
}
