package br.com.ibeans.receivables.adapter.in.web.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.domain.ReceivableType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TypeRateRequest(
        @Schema(description = "Tipo do recebível.", example = "DUPLICATA_MERCANTIL")
        @NotNull ReceivableType type,
        @Schema(description = "Spread em formato decimal; 0.005 representa 0,5%.", example = "0.005")
        @NotNull @DecimalMin("0.0") BigDecimal spread,
        @Schema(description = "Chave da estratégia de precificação.", example = "STANDARD")
        @NotBlank String strategyKey,
        @Schema(description = "Início de vigência da configuração, sem fuso horário.", example = "2026-09-01T00:00:00")
        @NotNull LocalDateTime validFrom
) {
}
