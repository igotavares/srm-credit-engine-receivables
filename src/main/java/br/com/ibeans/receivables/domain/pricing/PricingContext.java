package br.com.ibeans.receivables.domain.pricing;

import java.math.BigDecimal;

public record PricingContext(
        BigDecimal faceValue,
        BigDecimal baseRate,
        BigDecimal spread,
        BigDecimal termMonths
) {
}
