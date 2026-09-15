package br.com.ibeans.receivables.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EffectiveRate(
        BigDecimal rate,
        LocalDateTime validFrom
) {
    public EffectiveRate {
        if (rate == null || rate.signum() < 0) {
            throw new IllegalArgumentException("Taxa inválida");
        }
    }
}
