package br.com.ibeans.receivables.domain.pricing;

import br.com.ibeans.receivables.domain.ExchangeRateQuote;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;

import java.math.BigDecimal;

public record PricingCalculation(
        BigDecimal termMonths,
        EffectiveRate baseRate,
        ReceivableTypeConfiguration typeConfiguration,
        String pricingStrategy,
        String calculationMethod,
        long calculationVersion,
        BigDecimal presentValue,
        Currency presentValueCurrency,
        BigDecimal discount,
        ExchangeRateQuote exchangeRate,
        BigDecimal finalAmount,
        Currency finalCurrency
) {
}
