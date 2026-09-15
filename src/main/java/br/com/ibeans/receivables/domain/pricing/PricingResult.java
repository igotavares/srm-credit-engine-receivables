package br.com.ibeans.receivables.domain.pricing;

import java.math.BigDecimal;

public record PricingResult(
        BigDecimal presentValue,
        BigDecimal discount
) {
}
