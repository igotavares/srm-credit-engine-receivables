package br.com.ibeans.receivables.adapter.in.web.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.domain.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BaseRateRequest(
        @Schema(description = "Moeda da operação.", example = "BRL")
        @NotNull Currency currency,
        @Schema(description = "Taxa base em formato decimal; 0.01 representa 1%.", example = "0.01")
        @NotNull @DecimalMin("0.0") BigDecimal rate,
        @Schema(description = "Início de vigência da configuração, sem fuso horário.", example = "2026-09-01T00:00:00")
        @NotNull LocalDateTime validFrom
) {
}
