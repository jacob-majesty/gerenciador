package com.gerenciador.projeto.service;

import com.gerenciador.projeto.client.MemberApiClient;
import com.gerenciador.projeto.dto.*;
import com.gerenciador.projeto.entity.Allocation;
import com.gerenciador.projeto.entity.Project;
import com.gerenciador.projeto.enums.ProjectStatus;
import com.gerenciador.projeto.enums.RiskLevel;
import com.gerenciador.projeto.exception.*;
import com.gerenciador.projeto.mapper.AllocationMapper;
import com.gerenciador.projeto.mapper.ProjectMapper;
import com.gerenciador.projeto.repository.AllocationRepository;
import com.gerenciador.projeto.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;


/**
 * Implementação do serviço de projetos, contendo as regras de negócio.
 * Gerencia a criação, leitura, atualização e exclusão de projetos,
 * além da alocação de membros e cálculo de risco.
 */

@Service
public class ProjectService implements IProjectService {

    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;
    private final ProjectMapper projectMapper;
    private final AllocationMapper allocationMapper;
    private final MemberApiClient memberApiClient; // Cliente para a API externa de membros

    public ProjectService(ProjectRepository projectRepository,
                          AllocationRepository allocationRepository,
                          ProjectMapper projectMapper,
                          AllocationMapper allocationMapper,
                          MemberApiClient memberApiClient) {
        this.projectRepository = projectRepository;
        this.allocationRepository = allocationRepository;
        this.projectMapper = projectMapper;
        this.allocationMapper = allocationMapper;
        this.memberApiClient = memberApiClient;
    }

    @Override
    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO projectRequestDTO) {
        validateMemberExists(projectRequestDTO.getManagerId());

        Project project = projectMapper.toEntity(projectRequestDTO);
        project = projectRepository.save(project);
        return mapProjectToResponseDTO(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado com ID: " + id));
        return mapProjectToResponseDTO(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDTO> getAllProjects(String name, String status, Long managerId, LocalDate startDateFrom, LocalDate startDateTo, Pageable pageable) {
        Specification<Project> spec = Specification.where(null);

        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (status != null && !status.isEmpty()) {
            try {
                ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase().replace(" ", "_"));
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), projectStatus));
            } catch (IllegalArgumentException e) {
                throw new InvalidStatusTransitionException("Status inválido: " + status);
            }
        }
        if (managerId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("managerId"), managerId));
        }
        if (startDateFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
        }
        if (startDateTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), startDateTo));
        }

        Page<Project> projectPage = projectRepository.findAll(spec, pageable);
        List<ProjectResponseDTO> dtoList = projectPage.getContent().stream()
                .map(this::mapProjectToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, projectPage.getTotalElements());
    }

    @Override
    @Transactional
    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO projectRequestDTO) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado com ID: " + id));

        if (projectRequestDTO.getManagerId() != null && !projectRequestDTO.getManagerId().equals(existingProject.getManagerId())) {
            validateMemberExists(projectRequestDTO.getManagerId());
        }

        projectMapper.updateProjectFromDto(projectRequestDTO, existingProject);

        if (projectRequestDTO.getStatus() != null) {
            ProjectStatus newStatus = ProjectStatus.valueOf(projectRequestDTO.getStatus().toUpperCase().replace(" ", "_"));
            if (existingProject.getStatus() != newStatus) {
                throw new InvalidStatusTransitionException("Alteração de status deve ser feita via endpoint PATCH /status.");
            }
        }

        Project updatedProject = projectRepository.save(existingProject);
        return mapProjectToResponseDTO(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponseDTO updateProjectStatus(Long id, ProjectStatusUpdateDTO statusUpdateDTO) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado com ID: " + id));

        ProjectStatus newStatus;
        try {
            newStatus = ProjectStatus.valueOf(statusUpdateDTO.getNewStatus().toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusTransitionException("Status inválido: " + statusUpdateDTO.getNewStatus());
        }

        if (!project.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Transição de status inválida de '" + project.getStatus().getDescription() +
                            "' para '" + newStatus.getDescription() + "'."
            );
        }

        project.setStatus(newStatus);
        if (newStatus == ProjectStatus.ENCERRADO && project.getActualEndDate() == null) {
            project.setActualEndDate(LocalDate.now());
        }

        project = projectRepository.save(project);
        return mapProjectToResponseDTO(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado com ID: " + id));

        if (project.getStatus() == ProjectStatus.EM_ANDAMENTO || project.getStatus() == ProjectStatus.ENCERRADO || project.getStatus() == ProjectStatus.PLANEJADO) {
            throw new ProjectDeletionException("Não é possível excluir o projeto com o status '" + project.getStatus().getDescription() + "'.");
        }
        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public ProjectResponseDTO allocateMembersToProject(Long projectId, List<Long> memberIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado com ID: " + projectId));

        if (memberIds == null || memberIds.isEmpty() || memberIds.size() > 10) {
            throw new MemberAllocationException("Deve alocar entre 1 e 10 membros por vez.");
        }

        for (Long memberId : memberIds) {
            MemberDTO member = getMemberFromExternalApi(memberId);
            if (!"funcionário".equalsIgnoreCase(member.getRole())) {
                throw new MemberAllocationException("Membro com ID " + memberId + " não é um funcionário e não pode ser alocado.");
            }

            if (allocationRepository.existsByProjectIdAndMemberId(projectId, memberId)) {
                throw new MemberAllocationException("Membro com ID " + memberId + " já está alocado neste projeto.");
            }

            List<ProjectStatus> excludedStatuses = List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO);
            long activeProjectsCount = projectRepository.findProjectsByAllocatedMemberAndStatusNotIn(memberId, excludedStatuses).size();

            if (activeProjectsCount >= 3) {
                throw new MemberAllocationException("Membro com ID " + memberId + " já está alocado em 3 projetos em andamento/planejado/iniciado.");
            }

            Allocation allocation = new Allocation(project, memberId);
            project.addAllocation(allocation);
        }

        Project updatedProject = projectRepository.save(project);
        return mapProjectToResponseDTO(updatedProject);
    }

    @Override
    @Transactional
    public void deallocateMemberFromProject(Long projectId, Long memberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado com ID: " + projectId));

        Optional<Allocation> allocationToRemove = project.getAllocations().stream()
                .filter(alloc -> Objects.equals(alloc.getMemberId(), memberId))
                .findFirst();

        if (allocationToRemove.isEmpty()) {
            throw new MemberAllocationException("Membro com ID " + memberId + " não está alocado neste projeto.");
        }

        project.removeAllocation(allocationToRemove.get());
        projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberAllocationDTO> getAllocatedMembers(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto não encontrado com ID: " + projectId));

        return project.getAllocations().stream()
                .map(allocation -> {
                    MemberAllocationDTO dto = allocationMapper.toDto(allocation);
                    try {
                        MemberDTO member = memberApiClient.getMemberById(allocation.getMemberId());
                        dto.setMemberName(member.getName());
                    } catch (Exception e) {
                        System.err.println("Erro ao buscar nome do membro " + allocation.getMemberId() + ": " + e.getMessage());
                        dto.setMemberName("[Nome indisponível]");
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public RiskLevel calculateRiskLevel(BigDecimal budget, LocalDate startDate, LocalDate forecastEndDate) {
        if (startDate == null || forecastEndDate == null) {
            return RiskLevel.BAIXO_RISCO;
        }

        long monthsDuration = ChronoUnit.MONTHS.between(startDate, forecastEndDate);

        if (budget.compareTo(new BigDecimal("100000")) <= 0 && monthsDuration <= 3) {
            return RiskLevel.BAIXO_RISCO;
        } else if ((budget.compareTo(new BigDecimal("100000")) > 0 && budget.compareTo(new BigDecimal("500000")) <= 0) ||
                (monthsDuration > 3 && monthsDuration <= 6)) {
            return RiskLevel.MEDIO_RISCO;
        } else if (budget.compareTo(new BigDecimal("500000")) > 0 || monthsDuration > 6) {
            return RiskLevel.ALTO_RISCO;
        }
        return RiskLevel.BAIXO_RISCO;
    }

    /**
     * Método auxiliar para mapear uma entidade Project para ProjectResponseDTO
     * e preencher dados adicionais como nome do gerente e membros alocados,
     * e calcular o nível de risco.
     * @param project Entidade Project.
     * @return DTO de resposta completo.
     */
    private ProjectResponseDTO mapProjectToResponseDTO(Project project) {
        ProjectResponseDTO responseDTO = projectMapper.toResponseDto(project);

        try {
            MemberDTO manager = memberApiClient.getMemberById(project.getManagerId());
            responseDTO.setManagerName(manager.getName());
        } catch (Exception e) {
            System.err.println("Erro ao buscar nome do gerente " + project.getManagerId() + ": " + e.getMessage());
            responseDTO.setManagerName("[Nome indisponível]");
        }

        List<MemberAllocationDTO> allocatedMembers = project.getAllocations().stream()
                .map(allocation -> {
                    MemberAllocationDTO memberDto = allocationMapper.toDto(allocation);
                    try {
                        MemberDTO member = memberApiClient.getMemberById(allocation.getMemberId());
                        memberDto.setMemberName(member.getName());
                    } catch (Exception e) {
                        System.err.println("Erro ao buscar nome do membro alocado " + allocation.getMemberId() + ": " + e.getMessage());
                        memberDto.setMemberName("[Nome indisponível]");
                    }
                    return memberDto;
                })
                .collect(Collectors.toList());
        responseDTO.setAllocatedMembers(allocatedMembers);

        responseDTO.setRiskLevel(calculateRiskLevel(project.getTotalBudget(), project.getStartDate(), project.getForecastEndDate()));

        return responseDTO;
    }

    /**
     * Valida se um membro existe na API externa de membros.
     * @param memberId ID do membro a ser validado.
     * @throws ExternalApiException Se o membro não for encontrado.
     */
    private MemberDTO validateMemberExists(Long memberId) {
        try {
            return memberApiClient.getMemberById(memberId);
        } catch (Exception e) {
            throw new ExternalApiException("Membro com ID " + memberId + " não encontrado na API externa ou serviço indisponível.", e);
        }
    }
    private MemberDTO getMemberFromExternalApi(Long memberId) {
        try {
            return memberApiClient.getMemberById(memberId);
        } catch (Exception e) {
            throw new ExternalApiException("Erro ao buscar membro " + memberId + " na API externa.", e);
        }
    }


}