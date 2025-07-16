package com.gerenciador.projeto.enums;

/**
 * Define os níveis de risco de um projeto.
 * [cite_start]O cálculo é dinâmico com base em orçamento e prazo. [cite: 8, 75]
 */
public enum RiskLevel {
    BAIXO_RISCO("Baixo Risco"),
    MEDIO_RISCO("Médio Risco"),
    ALTO_RISCO("Alto Risco");

    private final String description;

    RiskLevel(String description) {
        this.description = description;
    }


    public String getDescription() {
        return description;
    }
}
