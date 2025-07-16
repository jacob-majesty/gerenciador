package com.mock.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

/**
 * Aplicação Mock da API de Membros.
 * Simula uma API externa para fornecer dados de membros.
 * Rodará na porta 8081.
 */
@SpringBootApplication
@RestController // Indica que esta classe contém endpoints REST
@RequestMapping("/api/membros") // Prefixo para todos os endpoints neste controller
public class MockApplication {

	// Mapa simples para simular um banco de dados de membros
	private final Map<Long, MemberDTO> members = new HashMap<>();

	public MockApplication() {
		// Popula alguns membros para teste
		members.put(1L, new MemberDTO(1L, "João Silva", "gerente"));
		members.put(101L, new MemberDTO(101L, "Maria Souza", "funcionário"));
		members.put(102L, new MemberDTO(102L, "Pedro Costa", "funcionário"));
		members.put(103L, new MemberDTO(103L, "Ana Oliveira", "funcionário"));
		members.put(2L, new MemberDTO(2L, "Carlos Pereira", "gerente")); // Outro gerente para teste
	}

	/**
	 * Endpoint para buscar um membro pelo ID.
	 * @param id ID do membro.
	 * @return ResponseEntity com o MemberDTO ou status 404 se não encontrado.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<MemberDTO> getMemberById(@PathVariable Long id) {
		MemberDTO member = members.get(id);
		if (member == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Retorna 404 se o membro não existir
		}
		return new ResponseEntity<>(member, HttpStatus.OK);
	}

	public static void main(String[] args) {
		SpringApplication.run(MockApplication.class, args);
	}
}
