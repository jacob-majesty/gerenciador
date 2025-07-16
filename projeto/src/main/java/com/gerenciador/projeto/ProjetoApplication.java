package com.gerenciador.projeto;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@OpenAPIDefinition(
		info = @Info(
				title = "API de Gerenciamento de Portfólio de Projetos",
				description = "Documentação da API REST para gerenciar projetos, alocações de membros e gerar relatórios de portfólio.",
				version = "v1.0", // Versão da sua API
				contact = @Contact(
						name = "Jacob",
						url = "https://github.com/jacob-majesty/gerenciador"
				)
		),
		externalDocs = @ExternalDocumentation(
				description = "Documentação Externa do Projeto de Portfólio",
				url = "https://github.com/jacob-majesty/gerenciador"
		)
)
public class ProjetoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetoApplication.class, args);
	}

}
