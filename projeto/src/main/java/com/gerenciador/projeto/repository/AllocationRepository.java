package com.gerenciador.projeto.repository;

import com.gerenciador.projeto.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade {@link Allocation}.
 * Estende {@link JpaRepository} para operações CRUD básicas de alocações.
 */
@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    /**
     * Verifica se uma alocação específica (projeto-membro) já existe.
     * @param projectId O ID do projeto.
     * @param memberId O ID do membro.
     * @return true se a alocação existir, false caso contrário.
     */
    boolean existsByProjectIdAndMemberId(Long projectId, Long memberId);
}
