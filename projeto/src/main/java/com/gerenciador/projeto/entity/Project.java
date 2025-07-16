package com.gerenciador.projeto.entity;

import com.gerenciador.projeto.enums.ProjectStatus;
import com.gerenciador.projeto.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set; // Usar Set para garantir unicidade e bom desempenho com relações

/**
 * Entidade JPA que representa um projeto no portfólio.
 * Contém informações como nome, datas, orçamento, status e a lista de membros alocados.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate forecastEndDate; // Data prevista de término

    private LocalDate actualEndDate; // Data real de término

    @Column(nullable = false, precision = 19, scale = 2) // Precision e Scale para BigDecimal
    private BigDecimal totalBudget;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Long managerId; // ID do gerente, referenciando a API externa de membros

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProjectStatus status;

    // Relação OneToMany com Allocation
    // mappedBy indica o campo na entidade Allocation que possui o mapeamento (o lado "muitos")
    // CascadeType.ALL significa que operações como persist, merge, remove serão propagadas para as alocações
    // orphanRemoval = true garante que alocações que não estão mais associadas a um projeto serão removidas
    // fetch = FetchType.LAZY para carregamento preguiçoso das alocações (melhor performance)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Allocation> allocations = new HashSet<>();

    // O RiskLevel não é um campo persistido, é calculado em tempo de execução
    @Transient // Indica que este campo não será mapeado para o banco de dados
    private RiskLevel riskLevel;


    /**
     * Adiciona uma alocação a este projeto.
     * Mantém a bidirecionalidade da relação, garantindo que a alocação aponte de volta para este projeto.
     * @param allocation A alocação a ser adicionada.
     */
    public void addAllocation(Allocation allocation) {
        this.allocations.add(allocation);
        allocation.setProject(this);
    }

    /**
     * Remove uma alocação deste projeto.
     * Mantém a bidirecionalidade da relação, desvinculando a alocação deste projeto.
     * @param allocation A alocação a ser removida.
     */
    public void removeAllocation(Allocation allocation) {
        this.allocations.remove(allocation);
        allocation.setProject(null); // Importante para desvincular
    }
}
