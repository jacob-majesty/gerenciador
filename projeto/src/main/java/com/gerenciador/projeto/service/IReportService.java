package com.gerenciador.projeto.service;

import com.gerenciador.projeto.dto.PortfolioSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Interface que define o contrato para as operações de serviço de relatórios.
 * Foca na geração de resumos e estatísticas do portfólio.
 */
@Tag(name = "Relatórios do Portfólio", description = "Operações para gerar relatórios e estatísticas gerais do portfólio de projetos.")
public interface IReportService {

    @Operation(summary = "Gera um relatório resumido do portfólio",
            description = "Fornece estatísticas agregadas sobre todos os projetos, incluindo quantidade por status, total orçado por status, média de duração de projetos encerrados e total de membros únicos alocados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PortfolioSummaryDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno ao gerar o relatório",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Erro ao calcular a média de duração de projetos encerrados...\"}"))),
            @ApiResponse(responseCode = "503", description = "Serviço de membros indisponível (pode impactar a contagem de membros únicos)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"timestamp\":\"...\",\"message\":\"Erro ao buscar membros na API externa...\"}")))
    })
    PortfolioSummaryDTO generatePortfolioSummary();
}