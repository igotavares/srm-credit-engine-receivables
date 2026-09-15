package br.com.ibeans.receivables.adapter.in.web.pricing;

import br.com.ibeans.receivables.adapter.in.web.handler.ProblemDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.PricingResponse;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.SimulationRequest;

import java.util.Optional;

@Tag(name = "Precificação", description = "Simulação de valores de recebíveis.")
public interface SwaggerPricing {

    @Operation(summary = "Simular precificação", description = "Calcula o valor presente e o valor de pagamento sem persistir um recebível. Requer configurações vigentes na data de referência.")
    @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PricingResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou parâmetros inválidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "404", description = "Recurso ou configuração não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "503", description = "Serviço de câmbio indisponível.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    Optional<PricingResponse> simulate(SimulationRequest request);

}
