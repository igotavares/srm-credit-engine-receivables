package br.com.ibeans.receivables.adapter.in.web.settlement;

import br.com.ibeans.receivables.adapter.in.web.handler.ProblemDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import br.com.ibeans.receivables.adapter.in.web.settlement.dto.SettlementRequest;
import br.com.ibeans.receivables.adapter.in.web.settlement.dto.SettlementResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Liquidações", description = "Liquidação idempotente de recebíveis.")
public interface SwaggerSettlement {

    @Operation(summary = "Liquidar recebível", description = "Calcula e registra a liquidação. Repetir a chave com o mesmo recebível e moeda retorna a liquidação anterior; reutilizá-la com outros dados ou liquidar novamente com outra chave viola uma regra de negócio (422).")
    @ApiResponse(responseCode = "200", description = "Liquidação recuperada por idempotência.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SettlementResponse.class)), headers = @Header(name = "Idempotency-Replayed", description = "Indica se a resposta foi recuperada de uma liquidação anterior.", schema = @Schema(type = "boolean", example = "true")))
    @ApiResponse(responseCode = "201", description = "Liquidação criada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SettlementResponse.class)), headers = @Header(name = "Idempotency-Replayed", description = "Indica se a resposta foi recuperada de uma liquidação anterior.", schema = @Schema(type = "boolean", example = "false")))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou parâmetros inválidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "404", description = "Recurso ou configuração não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de persistência ou atualização concorrente.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "503", description = "Serviço de câmbio indisponível.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    ResponseEntity<SettlementResponse> settle(
            @Parameter(description = "Identificador do recebível.", required = true) UUID receivableId,
            @Parameter(description = "Chave única da requisição, não vazia, com até 120 caracteres.", required = true, example = "liquidacao-001", schema = @Schema(maxLength = 120)) String idempotencyKey,
            SettlementRequest request
    );

}
