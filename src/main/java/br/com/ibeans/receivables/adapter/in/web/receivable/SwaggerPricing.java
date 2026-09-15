package br.com.ibeans.receivables.adapter.in.web.receivable;

import br.com.ibeans.receivables.adapter.in.web.handler.ProblemDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import br.com.ibeans.receivables.adapter.in.web.receivable.dto.CreateReceivableRequest;
import br.com.ibeans.receivables.adapter.in.web.receivable.dto.ReceivableResponse;
import br.com.ibeans.receivables.adapter.in.web.receivable.dto.UpdateReceivableRequest;

import java.util.Optional;
import java.util.UUID;

@Tag(name = "Recebíveis", description = "Cadastro e consulta de recebíveis.")
public interface SwaggerPricing {

    @Operation(summary = "Criar recebível", description = "Cadastra um recebível com os dados do cedente e do título.")
    @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReceivableResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou parâmetros inválidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de persistência ou atualização concorrente.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    Optional<ReceivableResponse> create(CreateReceivableRequest request);

    @Operation(summary = "Atualizar recebível", description = "Atualiza valor de face, vencimento e tipo de um recebível.")
    @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReceivableResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou parâmetros inválidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "404", description = "Recurso ou configuração não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de persistência ou atualização concorrente.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    Optional<ReceivableResponse> update(@Parameter(description = "Identificador do recebível.", required = true) UUID id, UpdateReceivableRequest request);

    @Operation(summary = "Consultar recebível", description = "Consulta um recebível pelo identificador.")
    @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReceivableResponse.class)))
    @ApiResponse(responseCode = "400", description = "Requisição inválida ou parâmetros inválidos.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "404", description = "Recurso ou configuração não encontrado.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor.", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetailResponse.class)))
    Optional<ReceivableResponse> find(@Parameter(description = "Identificador do recebível.", required = true) UUID id);

}
