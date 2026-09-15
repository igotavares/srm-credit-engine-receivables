package br.com.ibeans.receivables.adapter.in.web.pricing;

import br.com.ibeans.receivables.adapter.in.web.handler.ProblemDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.BaseRateRequest;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.BaseRateResponse;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.TypeRateRequest;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.TypeRateResponse;

import java.util.Optional;
import java.time.LocalDateTime;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.ReceivableType;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "Configurações de precificação", description = "Cadastro de taxas e estratégias com início de vigência.")
public interface SaggerPricingConfiguration {

    @Operation(summary = "Consultar taxa base vigente", description = "Retorna a configuração da moeda com o início de vigência mais recente menor ou igual à data e hora informadas.")
    @ApiResponse(responseCode = "200", description = "Configuração vigente encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseRateResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou parâmetros inválidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "404", description = "Configuração vigente não encontrada.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    BaseRateResponse findBaseRate(
            Currency currency,
            @Parameter(description = "Data e hora da consulta, sem fuso horário.", example = "2026-09-14T10:00:00", required = true) LocalDateTime at
    );

    @Operation(summary = "Consultar configuração vigente por tipo", description = "Retorna a configuração do tipo de recebível com o início de vigência mais recente menor ou igual à data e hora informadas.")
    @ApiResponse(responseCode = "200", description = "Configuração vigente encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TypeRateResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou parâmetros inválidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "404", description = "Configuração vigente não encontrada.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    TypeRateResponse findReceivableTypeConfiguration(
            ReceivableType type,
            @Parameter(description = "Data e hora da consulta, sem fuso horário.", example = "2026-09-14T10:00:00", required = true) LocalDateTime at
    );

    @Operation(summary = "Cadastrar taxa base", description = "Cadastra uma taxa base por moeda e data de início de vigência.")
    @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseRateResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou parâmetros inválidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de persistência ou atualização concorrente.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    BaseRateResponse addBaseRate(BaseRateRequest request);

    @Operation(summary = "Cadastrar configuração por tipo", description = "Cadastra spread e estratégia de cálculo para um tipo de recebível.")
    @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TypeRateResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou parâmetros inválidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de persistência ou atualização concorrente.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    Optional<TypeRateResponse> addTypeRate(TypeRateRequest request);

}
