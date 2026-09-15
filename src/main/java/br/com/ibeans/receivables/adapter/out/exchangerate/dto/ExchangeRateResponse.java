package br.com.ibeans.receivables.adapter.out.exchangerate.dto;

import java.time.LocalDateTime;

public record ExchangeRateResponse(
        java.util.UUID id,
        String baseCurrency,
        String quoteCurrency,
        String rate,
        LocalDateTime validFrom
) {
}
