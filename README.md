

# Sistema de Gerenciamento de Portfólio de Projetos

## Visão Geral
Este projeto visa desenvolver um sistema para gerenciar o portfólio de projetos de uma empresa, permitindo o acompanhamento completo do ciclo de vida de cada projeto, desde a análise de viabilidade até a finalização. O sistema oferece funcionalidades para gerenciamento de equipe, orçamento e risco.

## Tecnologias e Arquitetura
- **Arquitetura**: MVC (Model-View-Controller)
- **Backend**: Spring Boot
- **Persistência**: JPA/Hibernate com PostgreSQL
- **Documentação**: [Swagger/OpenAPI](#) (link a ser adicionado após deploy)
- **Segurança**: Spring Security
- **Containerização**: Docker
- **Boas práticas**: Clean Code, Princípios SOLID, DTOs e tratamento global de exceções

## Funcionalidades Principais
### Gestão de Projetos
- CRUD completo de projetos com campos como: nome, data de início, previsão de término, orçamento, descrição, status e gerente responsável
- Status fixos do projeto com transições controladas (sem pular etapas)
- Cálculo dinâmico da classificação de risco (baseado em orçamento e prazo)

### Gestão de Equipe
- Associação de membros ao projeto (somente membros com atribuição "funcionário")
- Integração com API REST externa mockada para cadastro e consulta de membros

### Relatórios
- Quantidade de projetos por status
- Total orçado por status
- Média de duração dos projetos encerrados
- Total de membros únicos alocados

## Configuração e Uso

### Banco de Dados (Docker)
```bash
docker-compose up -d
```
- `up`: Inicia os serviços definidos no docker-compose.yml
- `-d`: Executa em modo "detached" (em segundo plano)

### Segurança (Spring Security)
Credenciais padrão para desenvolvimento:
- **Usuário**: user
- **Senha**: userd123

### Documentação da API
A documentação completa dos endpoints está disponível via Swagger/OpenAPI:  
http://localhost:8080/swagger-ui.html

### Front-end
O arquivo `index.html` inclui:
- Formulário de login personalizado
- Tratamento de erros de autenticação (evita pop-up nativo do navegador)
- Redirecionamento automático em caso de credenciais inválidas

## Requisitos Atendidos
- [x] Arquitetura MVC com Spring Boot
- [x] Persistência com JPA/Hibernate e PostgreSQL
- [x] Documentação Swagger/OpenAPI
- [x] Tratamento global de exceções
- [x] Segurança básica com Spring Security
