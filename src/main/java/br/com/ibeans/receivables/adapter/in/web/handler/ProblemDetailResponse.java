package br.com.ibeans.receivables.adapter.in.web.handler;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.util.List;

@Schema(name = "ProblemDetailResponse", description = "Erro da API no formato Problem Detail, com código e correlação do trace.")
public record ProblemDetailResponse(
        @Schema(description = "URI que identifica o tipo do problema.", example = "about:blank")
        URI type,

        @Schema(description = "Título resumido do erro.", example = "Requisição inválida")
        String title,

        @Schema(description = "Status HTTP da resposta.", example = "400", minimum = "400", maximum = "599")
        int status,

        @Schema(description = "Descrição do erro nesta ocorrência.", example = "Um ou mais campos estão inválidos.")
        String detail,

        @Schema(description = "URI da requisição que originou o erro.", example = "/api/v1/receivables")
        URI instance,

        @Schema(description = "Código da regra de negócio ou EXRA seguido do status HTTP para erros genéricos.", example = "EXRA400")
        String code,

        @Schema(description = "Identificador do trace para correlação com logs. Presente quando há um span ativo.",
                example = "0123456789abcdef0123456789abcdef", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String traceId,

        @Schema(description = "Violações encontradas na validação da requisição. Presente apenas em erros de validação.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<Violacao> violacoes
) { }
