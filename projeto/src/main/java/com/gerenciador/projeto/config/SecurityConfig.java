package com.gerenciador.projeto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança para a aplicação, utilizando Spring Security.
 * Define usuários em memória, proteção de endpoints e liberação do Swagger UI.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configura um UserDetailsService em memória para autenticação.
    // O usuário 'user' e senha 'user123' são hardcoded aqui.
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("USER")
                .password(passwordEncoder.encode("user123")) // Codifica a senha antes de armazenar
                .roles("USER") // Você pode definir roles como "ADMIN", "USER", etc.
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs REST
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acesso público ao Swagger UI e à documentação OpenAPI
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**" // Necessário para carregar recursos do Swagger UI
                        ).permitAll() // Libera esses caminhos
                        // Todas as outras requisições para /api/** exigem autenticação
                        .requestMatchers("/api/**").authenticated()
                        // Se você tiver outros endpoints que não sejam /api/**, pode definir regras específicas ou tornar o resto autenticado
                        .anyRequest().authenticated()
                )
                .httpBasic(org.springframework.security.config.Customizer.withDefaults()) // Habilita autenticação Basic HTTP
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Para APIs RESTful, sem estado de sessão

        return http.build();
    }
}