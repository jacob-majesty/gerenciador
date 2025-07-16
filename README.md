# Sistema de Gerenciamento de Portfólio de Projetos

Este projeto visa desenvolver um sistema para gerenciar o portfólio de projetos de uma empresa, permitindo o acompanhamento completo do ciclo de vida de cada projeto, desde a análise de viabilidade até a finalização. O sistema oferece funcionalidades para gerenciamento de equipe, orçamento e risco.

## Requisitos

- **Arquitetura MVC**
- **Spring Boot** como framework principal
- **JPA + Hibernate** para persistência de dados
- **Banco de Dados PostgreSQL**
- **Clean Code** e **Princípios SOLID** aplicados
- **DTOs** e **mapeamento** entre entidades
- **Swagger/OpenAPI** para documentação dos endpoints
- **Tratamento global de exceções**
- **Testes unitários** com cobertura mínima de 70% nas regras de negócio
- **Segurança básica** com **Spring Security** (usuário/senha hardcoded ou em memória)

## Funcionalidades

- CRUD completo de projetos com campos como: nome, data de início, previsão de término, orçamento, descrição, status, e gerente responsável.
- Cálculo dinâmico da classificação de risco do projeto, com base em orçamento e prazo.
- Status fixos do projeto, com transições controladas e sem possibilidade de pular etapas.
- Associação de membros ao projeto (somente membros com a atribuição “funcionário”).
- API REST externa mockada para cadastro e consulta de membros.
- Relatório resumido do portfólio com:
    - Quantidade de projetos por status
    - Total orçado por status
    - Média de duração dos projetos encerrados
    - Total de membros únicos alocados

## Tecnologias

- **Spring Boot**
- **JPA** / **Hibernate**
- **PostgreSQL**
- **Swagger/OpenAPI**
- **Spring Security**
- **Docker**
