package br.com.ibeans.receivables.adapter.in.web.settlement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ReportResponse(
        @Schema(description = "Liquidações da página atual.")
        List<ReportItemResponse > items,
        @Schema(description = "Total de liquidações que correspondem aos filtros.", example = "1")
        long total,
        @Schema(description = "Índice da página, começando em zero.", example = "0")
        int page,
        @Schema(description = "Tamanho da página.", example = "20")
        int size
) {
}
