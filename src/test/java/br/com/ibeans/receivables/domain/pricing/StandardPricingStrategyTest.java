package br.com.ibeans.receivables.domain.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class StandardPricingStrategyTest {

    private final StandardPricingStrategy strategy = new StandardPricingStrategy();

    @Test
    void c1DuplicataDeveBaterGoldenCase() {
        var result = strategy.calculate(new PricingContext(
                new BigDecimal("100000.00"),
                new BigDecimal("0.01"),
                new BigDecimal("0.015"),
                new BigDecimal("3")
        ));

        assertThat(result.presentValue()).isEqualByComparingTo("92859.94");
        assertThat(result.discount()).isEqualByComparingTo("7140.06");
    }

    @Test
    void c2ChequeDeveBaterGoldenCase() {
        var result = strategy.calculate(new PricingContext(
                new BigDecimal("25000.00"),
                new BigDecimal("0.01"),
                new BigDecimal("0.025"),
                new BigDecimal("2")
        ));

        assertThat(result.presentValue()).isEqualByComparingTo("23337.77");
        assertThat(result.discount()).isEqualByComparingTo("1662.23");
    }
}
