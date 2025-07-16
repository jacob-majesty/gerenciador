package com.gerenciador.projeto.repository;

import com.gerenciador.projeto.entity.Project;
import com.gerenciador.projeto.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para a entidade {@link Project}.
 * Estende {@link JpaRepository} para operações CRUD básicas
 * e {@link JpaSpecificationExecutor} para consultas dinâmicas.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    /**
     * Busca projetos nos quais um determinado membro está alocado,
     * excluindo projetos com status específicos.
     * Usado para verificar a regra de limite de 3 projetos por membro.
     * @param memberId O ID do membro.
     * @param excludedStatuses Uma lista de status a serem excluídos da busca.
     * @return Uma lista de projetos onde o membro está alocado e que não possuem os status excluídos.
     */
    @Query("SELECT p FROM Project p JOIN p.allocations a WHERE a.memberId = :memberId AND p.status NOT IN :excludedStatuses")
    List<Project> findProjectsByAllocatedMemberAndStatusNotIn(
            @Param("memberId") Long memberId,
            @Param("excludedStatuses") List<ProjectStatus> excludedStatuses);
}
