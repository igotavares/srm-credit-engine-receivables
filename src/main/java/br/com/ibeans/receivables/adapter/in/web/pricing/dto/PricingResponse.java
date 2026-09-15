package br.com.ibeans.receivables.adapter.in.web.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.pricing.PricingCalculation;

import java.time.LocalDateTime;

public record PricingResponse(
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
        long calculationVersion,
        @Schema(description = "Valor presente na moeda do recebível.", example = "8363.87")
        String presentValue,
        @Schema(description = "Moeda do valor presente.", example = "BRL")
        Currency presentValueCurrency,
        @Schema(description = "Diferença entre valor nominal e valor presente.", example = "1636.13")
        String discount,
        @Schema(description = "Cotação aplicada; ausente quando não há conversão de moeda.", example = "5.00")
        String exchangeRate,
        @Schema(description = "Data e hora da cotação; ausente quando não há conversão, sem fuso horário.", example = "2026-09-14T10:00:00")
        LocalDateTime exchangeRateReferenceDate,
        @Schema(description = "Valor final na moeda do pagamento.", example = "8363.87")
        String finalAmount,
        @Schema(description = "Moeda do pagamento.", example = "BRL")
        Currency finalCurrency
) { }
