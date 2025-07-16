package com.gerenciador.projeto.dto;

/**
 * DTO para representar um membro da API externa mockada.
 * Usado para comunicação com o serviço de membros.
 */
public class MemberDTO {

    private Long id;
    private String name;
    private String role; // atribuição/cargo do membro [cite: 80]

    public MemberDTO(long id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
