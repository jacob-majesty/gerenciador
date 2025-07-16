package com.gerenciador.projeto.enums;

/**
 * Define os possíveis status de um projeto e suas regras de transição.
 * A ordem dos status é importante para a validação sequencial.
 */
public enum ProjectStatus {
    EM_ANALISE("Em Análise", 0),
    ANALISE_REALIZADA("Análise Realizada", 1),
    ANALISE_APROVADA("Análise Aprovada", 2),
    INICIADO("Iniciado", 3),
    PLANEJADO("Planejado", 4),
    EM_ANDAMENTO("Em Andamento", 5),
    ENCERRADO("Encerrado", 6),
    CANCELADO("Cancelado", 7); // O cancelamento pode ocorrer a qualquer momento [cite: 11]

    private final String description;
    private final int order; // Ordem sequencial para validação de transição [cite: 64]

    /**
     * Construtor para inicializar o status com uma descrição e ordem.
     * @param description A descrição legível do status.
     * @param order A ordem sequencial do status.
     */
    ProjectStatus(String description, int order) {
        this.description = description;
        this.order = order;
    }

    public String getDescription() {
        return description;
    }

    public int getOrder() {
        return order;
    }

    //Verifica se é possível fazer a transição para um novo status.

    public boolean canTransitionTo(ProjectStatus newStatus) {
        // Cancelamento pode ser aplicado a qualquer momento
        if (newStatus == CANCELADO) {
            return true;
        }
        // A regra de fluxo do status é sequencial
        return newStatus.getOrder() == this.order + 1;
    }
}
