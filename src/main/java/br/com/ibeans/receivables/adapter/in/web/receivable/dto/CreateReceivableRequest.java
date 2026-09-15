package br.com.ibeans.receivables.adapter.in.web.receivable.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.ReceivableType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateReceivableRequest(
        @Schema(description = "Identificador do cedente.", example = "CEDENTE-001")
        @NotBlank String assignorId,
        @Schema(description = "Valor nominal do recebível.", example = "10000.00")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal faceValue,
        @Schema(description = "Moeda da operação.", example = "BRL")
        @NotNull Currency currency,
        @Schema(description = "Data de aquisição.", example = "2026-09-14")
        @NotNull LocalDate acquisitionDate,
        @Schema(description = "Data de vencimento.", example = "2027-09-14")
        @NotNull LocalDate maturityDate,
        @Schema(description = "Tipo do recebível.", example = "DUPLICATA_MERCANTIL")
        @NotNull ReceivableType type
) {
}
