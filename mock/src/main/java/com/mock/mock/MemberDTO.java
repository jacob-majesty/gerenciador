package com.mock.mock;

/**
 * DTO para representar um membro na API Mock.
 * Deve ser o mesmo DTO usado no cliente Feign da aplicação principal.
 */
public class MemberDTO {
    private Long id;
    private String name;
    private String role;

    public MemberDTO(Long id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    // Getters e Setters (pode usar Lombok aqui também, se configurado)
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

