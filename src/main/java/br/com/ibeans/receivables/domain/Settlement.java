package br.com.ibeans.receivables.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Settlement(
        UUID id,
        UUID receivableId,
        String assignorId,
        BigDecimal faceValue,
        BigDecimal termMonths,
        BigDecimal baseRate,
        LocalDateTime baseRateReferenceDate,
        BigDecimal spread,
        LocalDateTime spreadReferenceDate,
        String pricingStrategy,
        String calculationMethod,
        Long calculationVersion,
        BigDecimal presentValue,
        Currency presentValueCurrency,
        BigDecimal exchangeRate,
        LocalDateTime exchangeRateReferenceDate,
        BigDecimal finalAmount,
        Currency finalCurrency,
        LocalDateTime settledAt,
        String idempotencyKey
) {
}
