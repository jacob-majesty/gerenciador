package com.gerenciador.projeto.mapper;

import com.gerenciador.projeto.dto.ProjectRequestDTO;
import com.gerenciador.projeto.dto.ProjectResponseDTO;
import com.gerenciador.projeto.entity.Project;
import com.gerenciador.projeto.enums.ProjectStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Componente responsável por mapear entre ProjectRequestDTO, ProjectResponseDTO e Project entity.
 * Implementa a lógica para calcular o nível de risco, mantendo o serviço focado na regra de negócio.
 */
@Component
public class ProjectMapper {

    /**
     * Converte um ProjectRequestDTO para uma entidade Project.
     * @param dto O DTO de requisição do projeto.
     * @return A entidade Project correspondente.
     */
    public Project toEntity(ProjectRequestDTO dto) {
        // Usa Optional para evitar NullPointerExceptions e tornar o código mais legível
        return Optional.ofNullable(dto).map(source -> {
            Project project = new Project();
            project.setName(source.getName());
            project.setStartDate(source.getStartDate());
            project.setForecastEndDate(source.getForecastEndDate());
            project.setActualEndDate(source.getActualEndDate());
            project.setTotalBudget(source.getTotalBudget());
            project.setDescription(source.getDescription());
            project.setManagerId(source.getManagerId());
            // Converte a String do DTO para o enum ProjectStatus
            project.setStatus(
                    Optional.ofNullable(source.getStatus())
                            .map(s -> ProjectStatus.valueOf(s.toUpperCase().replace(" ", "_")))
                            .orElse(ProjectStatus.EM_ANALISE) // Define um status padrão se nulo
            );
            return project;
        }).orElse(null);
    }

    /**
     * Converte uma entidade Project para um ProjectResponseDTO.
     * @param project A entidade Project.
     * @return O DTO de resposta do projeto.
     */
    public ProjectResponseDTO toResponseDto(Project project) {
        return Optional.ofNullable(project).map(source -> {
            ProjectResponseDTO dto = new ProjectResponseDTO();
            dto.setId(source.getId());
            dto.setName(source.getName());
            dto.setStartDate(source.getStartDate());
            dto.setForecastEndDate(source.getForecastEndDate());
            dto.setActualEndDate(source.getActualEndDate());
            dto.setTotalBudget(source.getTotalBudget());
            dto.setDescription(source.getDescription());
            dto.setManagerId(source.getManagerId());
            dto.setStatus(source.getStatus());
            return dto;
        }).orElse(null);
    }

    /**
     * Atualiza os campos de uma entidade Project com base nos dados de um ProjectRequestDTO.
     * Este método é usado para operações de PUT/PATCH onde apenas alguns campos são alterados.
     * @param dto O DTO de requisição com os dados atualizados.
     * @param project A entidade Project a ser atualizada.
     */
    public void updateProjectFromDto(ProjectRequestDTO dto, Project project) {
        if (dto == null || project == null) {
            return;
        }
        // Utiliza Optional para aplicar atualizações apenas se o campo no DTO não for nulo
        Optional.ofNullable(dto.getName()).ifPresent(project::setName);
        Optional.ofNullable(dto.getStartDate()).ifPresent(project::setStartDate);
        Optional.ofNullable(dto.getForecastEndDate()).ifPresent(project::setForecastEndDate);
        // actualEndDate pode ser null, então atribuímos diretamente
        project.setActualEndDate(dto.getActualEndDate());
        Optional.ofNullable(dto.getTotalBudget()).ifPresent(project::setTotalBudget);
        Optional.ofNullable(dto.getDescription()).ifPresent(project::setDescription);
        Optional.ofNullable(dto.getManagerId()).ifPresent(project::setManagerId);
        // O status é atualizado via um endpoint PATCH específico, não via este PUT genérico
    }


}