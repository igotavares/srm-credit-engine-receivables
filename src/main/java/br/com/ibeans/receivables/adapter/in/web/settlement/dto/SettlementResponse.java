package br.com.ibeans.receivables.adapter.in.web.settlement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.Settlement;

import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementResponse(
        @Schema(description = "Identificador do recurso.", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Identificador do recebível.", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID receivableId,
        @Schema(description = "Identificador do cedente.", example = "CEDENTE-001")
        String assignorId,
        @Schema(description = "Valor nominal do recebível.", example = "10000.00")
        String faceValue,
        @Schema(description = "Prazo do recebível em meses.", example = "12")
        String termMonths,
        @Schema(description = "Taxa base aplicada em formato decimal.", example = "0.01")
        String baseRate,
        @Schema(description = "Data e hora de vigência da taxa base, sem fuso horário.", example = "2026-09-14T10:00:00")
        LocalDateTime baseRateReferenceDate,
        @Schema(description = "Spread em formato decimal; 0.005 representa 0,5%.", example = "0.005")
        String spread,
        @Schema(description = "Data e hora de vigência do spread, sem fuso horário.", example = "2026-09-14T10:00:00")
        LocalDateTime spreadReferenceDate,
        @Schema(description = "Estratégia utilizada no cálculo.", example = "STANDARD")
        String pricingStrategy,
        @Schema(description = "Fórmula utilizada no cálculo.", example = "PV=FV/(1+BASE_RATE+SPREAD)^TERM")
        String calculationMethod,
        @Schema(description = "Versão do cálculo aplicado.", example = "1")
        Long calculationVersion,
        @Schema(description = "Valor presente na moeda do recebível.", example = "8363.87")
        String presentValue,
        @Schema(description = "Moeda do valor presente.", example = "BRL")
        Currency presentValueCurrency,
        @Schema(description = "Cotação aplicada; ausente quando não há conversão de moeda.", example = "5.00")
        String exchangeRate,
        @Schema(description = "Data e hora da cotação; ausente quando não há conversão, sem fuso horário.", example = "2026-09-14T10:00:00")
        LocalDateTime exchangeRateReferenceDate,
        @Schema(description = "Valor final na moeda do pagamento.", example = "8363.87")
        String finalAmount,
        @Schema(description = "Moeda do pagamento.", example = "BRL")
        Currency finalCurrency,
        @Schema(description = "Data e hora da liquidação, sem fuso horário.", example = "2026-09-14T10:00:00")
        LocalDateTime settledAt
) {
    public static SettlementResponse from(Settlement s) {
        return new SettlementResponse(
                s.id(),
                s.receivableId(),
                s.assignorId(),
                s.faceValue().toPlainString(),
                s.termMonths().toPlainString(),
                s.baseRate().toPlainString(),
                s.baseRateReferenceDate(),
                s.spread().toPlainString(),
                s.spreadReferenceDate(),
                s.pricingStrategy(),
                s.calculationMethod(),
                s.calculationVersion(),
                s.presentValue().toPlainString(),
                s.presentValueCurrency(),
                s.exchangeRate() != null ? s.exchangeRate().toPlainString() : null,
                s.exchangeRateReferenceDate(),
                s.finalAmount().toPlainString(),
                s.finalCurrency(),
                s.settledAt()
        );
    }
}
