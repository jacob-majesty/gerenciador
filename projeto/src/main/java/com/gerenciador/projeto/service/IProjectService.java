package com.gerenciador.projeto.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gerenciador.projeto.dto.MemberAllocationDTO;
import com.gerenciador.projeto.dto.ProjectRequestDTO;
import com.gerenciador.projeto.dto.ProjectResponseDTO;
import com.gerenciador.projeto.dto.ProjectStatusUpdateDTO;
import com.gerenciador.projeto.enums.RiskLevel;
import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface que define o contrato para as operações de serviço de projetos.
 * Garante a Inversão de Dependência e facilita a troca de implementações.
 */
@Tag(name = "Gerenciamento de Projetos", description = "Operações para criar, buscar, atualizar e gerenciar projetos.")
public interface IProjectService {

    @Operation(summary = "Cria um novo projeto",
            description = "Adiciona um novo projeto ao portfólio, validando a existência do gerente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida ou gerente não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Gerente com ID X não encontrado...\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Membro com ID X não encontrado na API externa...\"}")))
    })
    ProjectResponseDTO createProject(
            @Parameter(description = "Dados para criação do projeto", required = true)
            ProjectRequestDTO projectRequestDTO);

    @Operation(summary = "Busca um projeto pelo ID",
            description = "Retorna os detalhes completos de um projeto, incluindo informações do gerente e membros alocados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto encontrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Projeto não encontrado com ID: X\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Erro ao buscar nome do gerente X...\"}")))
    })
    ProjectResponseDTO getProjectById(
            @Parameter(description = "ID do projeto a ser buscado", example = "1", required = true)
            Long id);

    @Operation(summary = "Lista todos os projetos com filtros e paginação",
            description = "Permite buscar projetos por nome, status, ID do gerente e intervalo de datas de início.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de projetos retornada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Status de projeto inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Status inválido: XYZ\"}")))
    })
    Page<ProjectResponseDTO> getAllProjects(
            @Parameter(description = "Filtra projetos por nome (case-insensitive, parcial)", example = "Meu Projeto") String name,
            @Parameter(description = "Filtra projetos por status", example = "EM_ANDAMENTO",
                    schema = @Schema(implementation = String.class, allowableValues = {"EM_ANALISE", "ANALISE_REALIZADA", "ANALISE_APROVADA", "INICIADO", "PLANEJADO", "EM_ANDAMENTO", "ENCERRADO", "CANCELADO"})) String status,
            @Parameter(description = "Filtra projetos por ID do gerente", example = "101") Long managerId,
            @Parameter(description = "Data de início mínima (dd/MM/yyyy)", example = "01/01/2023") @JsonFormat(pattern = "dd/MM/yyyy") LocalDate startDateFrom,
            @Parameter(description = "Data de início máxima (dd/MM/yyyy)", example = "31/12/2023") @JsonFormat(pattern = "dd/MM/yyyy") LocalDate startDateTo,
            @Parameter(description = "Configurações de paginação e ordenação") Pageable pageable);

    @Operation(summary = "Atualiza os dados de um projeto existente",
            description = "Altera informações como nome, datas, orçamento e descrição de um projeto. A mudança de status deve ser feita via PATCH.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida ou tentativa de mudar status via PUT",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Alteração de status deve ser feita via endpoint PATCH /status.\" }"))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Projeto não encontrado com ID: X\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Membro com ID X não encontrado na API externa...\"}")))
    })
    ProjectResponseDTO updateProject(
            @Parameter(description = "ID do projeto a ser atualizado", example = "1", required = true)
            Long id,
            @Parameter(description = "Novos dados do projeto", required = true)
            ProjectRequestDTO projectRequestDTO);

    @Operation(summary = "Atualiza o status de um projeto",
            description = "Altera o status do projeto, aplicando regras de transição sequencial ou permitindo o cancelamento a qualquer momento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do projeto atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Status inválido ou transição não permitida",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Transição de status inválida de 'Em Análise' para 'Encerrado'.\"}"))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Projeto não encontrado com ID: X\"}")))
    })
    ProjectResponseDTO updateProjectStatus(
            @Parameter(description = "ID do projeto", example = "1", required = true)
            Long id,
            @Parameter(description = "Novo status do projeto", required = true)
            ProjectStatusUpdateDTO statusUpdateDTO);

    @Operation(summary = "Exclui um projeto",
            description = "Remove um projeto do sistema. A exclusão é permitida apenas se o projeto não estiver em 'Em Andamento', 'Encerrado' ou 'Planejado'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Projeto excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Impossível excluir projeto devido ao status",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Não é possível excluir o projeto com o status 'Em Andamento'.\"}"))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Projeto não encontrado com ID: X\"}")))
    })
    void deleteProject(
            @Parameter(description = "ID do projeto a ser excluído", example = "1", required = true)
            Long id);

    @Operation(summary = "Aloca membros a um projeto",
            description = "Associa um ou mais membros a um projeto. Validações incluem: mínimo 1, máximo 10 membros por vez; apenas 'funcionários'; e um membro não pode estar em mais de 3 projetos em andamento/planejado/iniciado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membros alocados com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida ou regra de alocação violada",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Membro com ID X já está alocado em 3 projetos em andamento/planejado/iniciado.\"}"))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Projeto não encontrado com ID: X\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Membro com ID X não encontrado na API externa...\"}")))
    })
    ProjectResponseDTO allocateMembersToProject(
            @Parameter(description = "ID do projeto", example = "1", required = true)
            Long projectId,
            @Parameter(description = "Lista de IDs dos membros a serem alocados", required = true, example = "[10, 11]")
            List<Long> memberIds);

    @Operation(summary = "Desaloca um membro de um projeto",
            description = "Remove a associação de um membro com um projeto específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Membro desalocado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Membro não alocado no projeto",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Membro com ID X não está alocado neste projeto.\"}"))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Projeto não encontrado com ID: X\"}")))
    })
    void deallocateMemberFromProject(
            @Parameter(description = "ID do projeto", example = "1", required = true)
            Long projectId,
            @Parameter(description = "ID do membro a ser desalocado", example = "10", required = true)
            Long memberId);

    @Operation(summary = "Lista membros alocados em um projeto",
            description = "Retorna todos os membros atualmente alocados a um projeto específico, incluindo seus nomes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de membros alocados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MemberAllocationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Projeto não encontrado com ID: X\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível (pode retornar nomes indisponíveis)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "[{\"memberId\": 10, \"memberName\": \"[Nome indisponível]\"}]")))
    })
    List<MemberAllocationDTO> getAllocatedMembers(
            @Parameter(description = "ID do projeto", example = "1", required = true)
            Long projectId);

    // Este método de cálculo de risco é interno ao serviço e não seria exposto diretamente via API.
    // A anotação @Operation seria mais comum em um método de Controller que expõe essa lógica diretamente.
    // Mantê-lo aqui na interface é válido para o contrato do serviço, mas sem anotações Swagger explícitas, pois não é um endpoint REST.
    RiskLevel calculateRiskLevel(BigDecimal budget, LocalDate startDate, LocalDate forecastEndDate);
}