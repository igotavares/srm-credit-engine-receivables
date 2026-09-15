package br.com.ibeans.receivables.adapter.in.web.settlement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.domain.Currency;
import jakarta.validation.constraints.NotNull;

public record SettlementRequest(
        @Schema(description = "Moeda da operação.", example = "BRL")
        @NotNull Currency currency
) {
}
