package com.gerenciador.projeto.mapper;

import com.gerenciador.projeto.dto.MemberAllocationDTO;
import com.gerenciador.projeto.entity.Allocation;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Componente responsável por mapear entre Allocation entity e MemberAllocationDTO.
 */
@Component
public class AllocationMapper {

    /**
     * Converte uma entidade Allocation para um MemberAllocationDTO.
     * O nome do membro será preenchido posteriormente por um serviço que consulta a API externa.
     * @param allocation A entidade Allocation.
     * @return O DTO de alocação de membro.
     */
    public MemberAllocationDTO toDto(Allocation allocation) {
        return Optional.ofNullable(allocation).map(source -> {
            MemberAllocationDTO dto = new MemberAllocationDTO();
            dto.setMemberId(source.getMemberId());
            // O memberName não é preenchido aqui, pois requer uma consulta à API externa de membros.
            // Isso será feito no ProjectService ou em um serviço de agregação de dados.
            return dto;
        }).orElse(null);
    }
}
