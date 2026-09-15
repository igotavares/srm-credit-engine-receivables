package br.com.ibeans.receivables.adapter.in.web.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;

import java.time.LocalDateTime;

public record BaseRateResponse(
        @Schema(description = "Moeda da operação.", example = "BRL")
        Currency currency,
        @Schema(description = "Taxa base em formato decimal; 0.01 representa 1%.", example = "0.01")
        String rate,
        @Schema(description = "Início de vigência da configuração, sem fuso horário.", example = "2026-09-01T00:00:00")
        LocalDateTime validFrom
) {
    public static BaseRateResponse from(Currency currency, EffectiveRate rate) {
        return new BaseRateResponse(currency, rate.rate().toPlainString(), rate.validFrom());
    }
}