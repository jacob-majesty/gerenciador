package com.gerenciador.projeto.controller;

import com.gerenciador.projeto.dto.PortfolioSummaryDTO;
import com.gerenciador.projeto.service.IReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para gerenciar operações de relatórios.
 * Expõe endpoints para gerar um resumo do portfólio de projetos.
 */
@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios e estatísticas do portfólio.")
public class ReportController {

    private final IReportService reportService;

    public ReportController(IReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Gera um relatório resumido do portfólio",
            description = "Fornece estatísticas agregadas sobre todos os projetos, incluindo quantidade por status, total orçado por status, média de duração de projetos encerrados e total de membros únicos alocados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioSummaryDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno ao gerar o relatório",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":500,\"error\":\"Internal Server Error\",\"message\":\"Erro ao calcular a média de duração...\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível (pode impactar a contagem de membros únicos)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"status\":503,\"error\":\"Service Unavailable\",\"message\":\"Erro ao buscar membros na API externa...\" }")))
    })
    @GetMapping("/resumo")
    public ResponseEntity<PortfolioSummaryDTO> generatePortfolioSummary() {
        PortfolioSummaryDTO summary = reportService.generatePortfolioSummary();
        return ResponseEntity.ok(summary);
    }
}
