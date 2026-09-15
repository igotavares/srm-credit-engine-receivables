package br.com.ibeans.receivables.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateQuoteTest {

    @Test
    void shouldCreateQuoteWithPositiveRate() {
        var validFrom = LocalDateTime.of(2026, 9, 14, 10, 0);

        var result = new ExchangeRateQuote(new BigDecimal("5.4321"), validFrom);

        assertThat(result.rate()).isEqualByComparingTo("5.4321");
        assertThat(result.validFrom()).isEqualTo(validFrom);
    }

    @Test
    void shouldRejectNullRate() {
        assertThatThrownBy(() -> new ExchangeRateQuote(null, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Taxa de câmbio inválida");
    }

    @Test
    void shouldRejectZeroRate() {
        assertThatThrownBy(() -> new ExchangeRateQuote(BigDecimal.ZERO, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Taxa de câmbio inválida");
    }

    @Test
    void shouldRejectNegativeRate() {
        assertThatThrownBy(() -> new ExchangeRateQuote(new BigDecimal("-0.01"), LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Taxa de câmbio inválida");
    }
}
