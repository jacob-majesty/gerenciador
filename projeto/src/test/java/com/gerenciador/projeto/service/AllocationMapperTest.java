package com.gerenciador.projeto.service;

import com.gerenciador.projeto.dto.MemberAllocationDTO;
import com.gerenciador.projeto.entity.Allocation;
import com.gerenciador.projeto.entity.Project;
import com.gerenciador.projeto.mapper.AllocationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes para AllocationMapper")
class AllocationMapperTest {

    private AllocationMapper allocationMapper;

    @BeforeEach
    void setUp() {
        allocationMapper = new AllocationMapper();
    }

    @Test
    @DisplayName("Deve mapear Allocation Entity para MemberAllocationDTO corretamente")
    void shouldMapEntityToDtoCorrectly() {
        Project project = new Project(); // Projeto simples, apenas para a relação
        project.setId(1L);

        Allocation allocation = new Allocation();
        allocation.setId(100L);
        allocation.setProject(project);
        allocation.setMemberId(20L);

        MemberAllocationDTO dto = allocationMapper.toDto(allocation);

        assertNotNull(dto);
        assertEquals(allocation.getMemberId(), dto.getMemberId());
        assertNull(dto.getMemberName()); // O nome do membro é preenchido no serviço, não no mapper
    }

    @Test
    @DisplayName("Deve retornar null ao mapear Allocation nula para DTO")
    void shouldReturnNullWhenMappingNullAllocationToDto() {
        assertNull(allocationMapper.toDto(null));
    }
}
