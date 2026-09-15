package br.com.ibeans.receivables.adapter.in.web.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.ReceivableType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SimulationRequest(
        @Schema(description = "Valor nominal do recebível.", example = "10000.00")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal faceValue,
        @Schema(description = "Moeda do recebível.", example = "BRL")
        @NotNull Currency receivableCurrency,
        @Schema(description = "Data de aquisição.", example = "2026-09-14")
        @NotNull LocalDate acquisitionDate,
        @Schema(description = "Data de vencimento.", example = "2027-09-14")
        @NotNull LocalDate maturityDate,
        @Schema(description = "Tipo do recebível.", example = "DUPLICATA_MERCANTIL")
        @NotNull ReceivableType type,
        @Schema(description = "Moeda desejada para o pagamento.", example = "BRL")
        @NotNull Currency paymentCurrency,
        @Schema(description = "Data e hora de referência das configurações; quando omitida, usa o instante atual.", example = "2026-09-14T10:00:00")
        LocalDateTime referenceDate
) {
}
