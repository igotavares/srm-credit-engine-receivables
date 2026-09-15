package br.com.ibeans.receivables.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateQuote(
        BigDecimal rate,
        LocalDateTime validFrom
) {
    public ExchangeRateQuote {
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalArgumentException("Taxa de câmbio inválida");
        }
    }
}
