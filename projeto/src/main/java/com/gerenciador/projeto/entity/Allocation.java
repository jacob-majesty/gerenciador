package com.gerenciador.projeto.entity;

import jakarta.persistence.*;

/**
 * Representa a entidade de alocação de membros em projetos.
 * Mapeada para a tabela 'allocations' no banco de dados.
 * Define o relacionamento N:M entre Projetos e Membros (o membro é externo).
 */
@Entity
@Table(name = "allocations")
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento muitos-para-um com a entidade Project
    // FetchType.LAZY: Carrega o projeto apenas quando necessário
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false) // Coluna de chave estrangeira
    private Project project;

    // ID do membro, que é um recurso externo (não persistido diretamente nesta tabela)
    @Column(nullable = false)
    private Long memberId;

    /**
     * Construtor padrão exigido pelo JPA.
     */
    public Allocation() {
    }

    /**
     * Construtor para facilitar a criação de uma nova alocação.
     * @param project O projeto ao qual o membro será alocado.
     * @param memberId O ID do membro a ser alocado.
     */
    public Allocation(Project project, Long memberId) {
        this.project = project;
        this.memberId = memberId;
    }

    // Getters e Setters para todos os atributos
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
