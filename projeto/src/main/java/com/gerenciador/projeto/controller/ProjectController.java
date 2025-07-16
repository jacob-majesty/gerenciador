package com.gerenciador.projeto.controller;

import com.gerenciador.projeto.dto.MemberAllocationDTO;
import com.gerenciador.projeto.dto.ProjectRequestDTO;
import com.gerenciador.projeto.dto.ProjectResponseDTO;
import com.gerenciador.projeto.dto.ProjectStatusUpdateDTO;
import com.gerenciador.projeto.service.IProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST para gerenciar operações relacionadas a projetos.
 * Expõe endpoints para CRUD, atualização de status e alocação/desalocação de membros.
 */
@RestController
@RequestMapping("/api/projetos")
@Tag(name = "Projetos", description = "Endpoints para gerenciar projetos e alocações de membros.")
public class ProjectController {

    private final IProjectService projectService;

    public ProjectController(IProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Cria um novo projeto",
            description = "Adiciona um novo projeto ao portfólio, validando a existência do gerente na API externa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados ausentes/inválidos) ou gerente não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Erro de validação\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Membro com ID X não encontrado na API externa ou serviço indisponível.\" }")))
    })
    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do projeto a ser criado", required = true)
            @Valid @RequestBody ProjectRequestDTO projectRequestDTO) {
        ProjectResponseDTO createdProject = projectService.createProject(projectRequestDTO);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @Operation(summary = "Busca um projeto pelo ID",
            description = "Retorna os detalhes completos de um projeto específico, incluindo o nome do gerente e os membros alocados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Projeto não encontrado com ID: X\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível (pode impactar o nome do gerente/membros)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Erro ao buscar nome do gerente X...\" }")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(
            @Parameter(description = "ID do projeto a ser buscado", example = "1", required = true)
            @PathVariable Long id) {
        ProjectResponseDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @Operation(summary = "Lista todos os projetos com filtros e paginação",
            description = "Retorna uma lista paginada de projetos. Permite filtrar por nome (parcial), status, ID do gerente e intervalo de datas de início.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de projetos retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Status de projeto inválido no filtro",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Status inválido: XYZ\"}")))
    })
    @GetMapping
    public ResponseEntity<Page<ProjectResponseDTO>> getAllProjects(
            @Parameter(description = "Filtra projetos por nome (case-insensitive, parcial)", example = "Portfólio")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filtra projetos por status", example = "EM_ANDAMENTO",
                    schema = @Schema(implementation = String.class, allowableValues = {"EM_ANALISE", "ANALISE_REALIZADA", "ANALISE_APROVADA", "INICIADO", "PLANEJADO", "EM_ANDAMENTO", "ENCERRADO", "CANCELADO"}))
            @RequestParam(required = false) String status,
            @Parameter(description = "Filtra projetos por ID do gerente", example = "101")
            @RequestParam(required = false) Long managerId,
            @Parameter(description = "Data de início mínima (formato dd/MM/yyyy)", example = "01/01/2023")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDateFrom,
            @Parameter(description = "Data de início máxima (formato dd/MM/yyyy)", example = "31/12/2023")
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDateTo,
            @Parameter(description = "Configurações de paginação e ordenação (ex: page=0&size=10&sort=name,asc)")
            Pageable pageable) {
        Page<ProjectResponseDTO> projects = projectService.getAllProjects(name, status, managerId, startDateFrom, startDateTo, pageable);
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Atualiza os dados de um projeto existente",
            description = "Altera informações como nome, datas, orçamento e descrição de um projeto. A mudança de status deve ser feita via endpoint PATCH.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (dados ausentes/inválidos) ou tentativa de mudar status via PUT",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Alteração de status deve ser feita via endpoint PATCH /status.\" }"))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Projeto não encontrado com ID: X\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Membro com ID X não encontrado na API externa ou serviço indisponível.\" }")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @Parameter(description = "ID do projeto a ser atualizado", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novos dados do projeto", required = true)
            @Valid @RequestBody ProjectRequestDTO projectRequestDTO) {
        ProjectResponseDTO updatedProject = projectService.updateProject(id, projectRequestDTO);
        return ResponseEntity.ok(updatedProject);
    }

    @Operation(summary = "Atualiza o status de um projeto",
            description = "Altera o status do projeto, aplicando regras de transição sequencial ou permitindo o cancelamento a qualquer momento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do projeto atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Status inválido ou transição não permitida",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Transição de status inválida de 'Em Análise' para 'Encerrado'.\"}"))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Projeto não encontrado com ID: X\"}")))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectResponseDTO> updateProjectStatus(
            @Parameter(description = "ID do projeto a ter o status atualizado", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novo status do projeto", required = true)
            @Valid @RequestBody ProjectStatusUpdateDTO statusUpdateDTO) {
        ProjectResponseDTO updatedProject = projectService.updateProjectStatus(id, statusUpdateDTO);
        return ResponseEntity.ok(updatedProject);
    }

    @Operation(summary = "Exclui um projeto",
            description = "Remove um projeto do sistema. A exclusão é permitida apenas se o projeto não estiver em 'Em Andamento', 'Encerrado' ou 'Planejado'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Projeto excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Impossível excluir projeto devido ao status atual",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Não é possível excluir o projeto com o status 'Em Andamento'.\"}"))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Projeto não encontrado com ID: X\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "ID do projeto a ser excluído", example = "1", required = true)
            @PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Aloca membros a um projeto",
            description = "Associa um ou mais membros a um projeto. Validações incluem: mínimo 1, máximo 10 membros por vez; apenas 'funcionários'; e um membro não pode estar em mais de 3 projetos em andamento/planejado/iniciado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membros alocados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida ou regra de alocação violada (ex: membro já alocado, cargo inválido, limite de projetos)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Membro com ID X já está alocado em 3 projetos em andamento/planejado/iniciado.\"}"))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Projeto não encontrado com ID: X\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Membro com ID X não encontrado na API externa ou serviço indisponível.\" }")))
    })
    @PostMapping("/{id}/membros")
    public ResponseEntity<ProjectResponseDTO> allocateMembersToProject(
            @Parameter(description = "ID do projeto para alocar membros", example = "1", required = true)
            @PathVariable("id") Long projectId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Lista de IDs dos membros a serem alocados", required = true)
            @RequestBody List<Long> memberIds) {
        ProjectResponseDTO updatedProject = projectService.allocateMembersToProject(projectId, memberIds);
        return ResponseEntity.ok(updatedProject);
    }

    @Operation(summary = "Desaloca um membro de um projeto",
            description = "Remove a associação de um membro com um projeto específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Membro desalocado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Membro não alocado no projeto",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Membro com ID X não está alocado neste projeto.\"}"))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Projeto não encontrado com ID: X\"}")))
    })
    @DeleteMapping("/{projectId}/membros/{memberId}")
    public ResponseEntity<Void> deallocateMemberFromProject(
            @Parameter(description = "ID do projeto para desalocar o membro", example = "1", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "ID do membro a ser desalocado", example = "10", required = true)
            @PathVariable Long memberId) {
        projectService.deallocateMemberFromProject(projectId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lista membros alocados em um projeto",
            description = "Retorna todos os membros atualmente alocados a um projeto específico, incluindo seus nomes completos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de membros alocados retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberAllocationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Projeto não encontrado com ID: X\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível (pode retornar nomes indisponíveis para membros)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "[{\"memberId\": 10, \"memberName\": \"[Nome indisponível]\"}]")))
    })
    @GetMapping("/{id}/membros")
    public ResponseEntity<List<MemberAllocationDTO>> getAllocatedMembers(
            @Parameter(description = "ID do projeto para listar os membros alocados", example = "1", required = true)
            @PathVariable("id") Long projectId) {
        List<MemberAllocationDTO> allocatedMembers = projectService.getAllocatedMembers(projectId);
        return ResponseEntity.ok(allocatedMembers);
    }
}