package com.gerenciador.projeto.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para representar um membro alocado em um projeto.
 * Usado tanto para entrada (ID do membro) quanto para saída (ID e nome para exibição).
 */
public class MemberAllocationDTO {

    @NotNull(message = "O ID do membro é obrigatório.")
    private Long memberId;
    private String memberName; // Nome do membro para exibição (obtido da API externa)

    // Getters e Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }
}
