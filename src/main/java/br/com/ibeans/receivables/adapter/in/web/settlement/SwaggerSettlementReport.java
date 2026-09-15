package br.com.ibeans.receivables.adapter.in.web.settlement;

import br.com.ibeans.receivables.adapter.in.web.handler.ProblemDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import br.com.ibeans.receivables.adapter.in.web.settlement.dto.ReportResponse;
import br.com.ibeans.receivables.domain.Currency;

import java.time.LocalDateTime;

@Tag(name = "Relatórios", description = "Consulta paginada de liquidações.")
public interface SwaggerSettlementReport {

    @Operation(summary = "Consultar relatório de liquidações", description = "Filtra liquidações por período, cedente e moeda. Sem resultados, retorna uma lista vazia.")
    @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou parâmetros inválidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    ReportResponse find(
            @Parameter(description = "Início do período de liquidação, sem fuso horário.", example = "2026-09-01T00:00:00") LocalDateTime from,
            @Parameter(description = "Fim do período de liquidação, sem fuso horário.", example = "2026-09-30T23:59:59") LocalDateTime to,
            @Parameter(description = "Identificador do cedente.", example = "CEDENTE-001") String assignorId,
            @Parameter(description = "Moeda do pagamento.", example = "BRL") Currency currency,
            @Parameter(description = "Índice da página, começando em zero.", schema = @Schema(minimum = "0", defaultValue = "0")) int page,
            @Parameter(description = "Quantidade de itens por página.", schema = @Schema(minimum = "1", maximum = "200", defaultValue = "20")) int size
    );

}
