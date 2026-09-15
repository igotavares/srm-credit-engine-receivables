package br.com.ibeans.receivables.adapter.in.web.pricing.mapper;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.PricingResponse;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;
import br.com.ibeans.receivables.domain.ExchangeRateQuote;
import br.com.ibeans.receivables.domain.ReceivableType;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;
import br.com.ibeans.receivables.domain.pricing.PricingCalculation;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PricingInMapperTest {

    private final PricingInMapper mapper = Mappers.getMapper(PricingInMapper.class);
    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2026, 9, 1, 10, 0);
    private static final LocalDateTime SPREAD_DATE = BASE_DATE.plusDays(2);
    private static final LocalDateTime EXCHANGE_DATE = BASE_DATE.plusDays(3);

    @Test
    void shouldMapAllFieldsWithExchangeRate() {
        var calculation = calculation(new ExchangeRateQuote(new BigDecimal("5.00"), EXCHANGE_DATE));

        assertThat(mapper.from(calculation)).isEqualTo(new PricingResponse(
                "3.50", "0.0100", BASE_DATE, "0.0250", SPREAD_DATE,
                "STANDARD", "COMPOUND", 7L, "900.00", Currency.BRL, "100.00",
                "5.00", EXCHANGE_DATE, "180.00", Currency.USD));
    }

    @Test
    void shouldMapCalculationWithoutCurrencyConversion() {
        var calculation = calculation(null);

        assertThat(mapper.from(calculation)).isEqualTo(new PricingResponse(
                "3.50", "0.0100", BASE_DATE, "0.0250", SPREAD_DATE,
                "STANDARD", "COMPOUND", 7L, "900.00", Currency.BRL, "100.00",
                null, null, "900.00", Currency.BRL));
    }

    @Test
    void shouldReturnNullForNullCalculation() {
        assertThat(mapper.from(null)).isNull();
    }

    @Test
    void shouldPreserveNullFieldsAndMissingConfigurations() {
        var calculation = new PricingCalculation(null, null, null, null, null, 0,
                null, null, null, null, null, null);

        assertThat(mapper.from(calculation)).isEqualTo(new PricingResponse(
                null, null, null, null, null, null, null, 0,
                null, null, null, null, null, null, null));
    }

    @Test
    void shouldRenderScientificNotationAsPlainDecimal() {
        assertThat(mapper.toPlainString(new BigDecimal("1E+6"))).isEqualTo("1000000");
        assertThat(mapper.toPlainString(new BigDecimal("1E-8"))).isEqualTo("0.00000001");
    }

    @Test
    void shouldPreserveDecimalScaleAndZero() {
        assertThat(mapper.toPlainString(new BigDecimal("123.4500"))).isEqualTo("123.4500");
        assertThat(mapper.toPlainString(BigDecimal.ZERO)).isEqualTo("0");
    }

    @Test
    void shouldReturnNullForNullDecimal() {
        assertThat(mapper.toPlainString(null)).isNull();
    }

    private PricingCalculation calculation(ExchangeRateQuote quote) {
        return new PricingCalculation(
                new BigDecimal("3.50"), new EffectiveRate(new BigDecimal("0.0100"), BASE_DATE),
                new ReceivableTypeConfiguration(ReceivableType.DUPLICATA_MERCANTIL,
                        new BigDecimal("0.0250"), "STANDARD", SPREAD_DATE),
                "STANDARD", "COMPOUND", 7L, new BigDecimal("900.00"), Currency.BRL,
                new BigDecimal("100.00"), quote,
                new BigDecimal(quote == null ? "900.00" : "180.00"),
                quote == null ? Currency.BRL : Currency.USD);
    }
}
