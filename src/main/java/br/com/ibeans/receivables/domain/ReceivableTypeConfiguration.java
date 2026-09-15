package br.com.ibeans.receivables.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReceivableTypeConfiguration(
        ReceivableType type,
        BigDecimal spread,
        String strategyKey,
        LocalDateTime validFrom
) {
    public ReceivableTypeConfiguration {
        if (spread == null || spread.signum() < 0) {
            throw new IllegalArgumentException("Spread inválido");
        }
        if (strategyKey == null || strategyKey.isBlank()) {
            throw new IllegalArgumentException("Strategy é obrigatória");
        }
    }
}
