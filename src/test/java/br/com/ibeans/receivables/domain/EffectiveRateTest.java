package br.com.ibeans.receivables.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EffectiveRateTest {

    @Test
    void shouldCreateRateWithPositiveValue() {
        var validFrom = LocalDateTime.of(2026, 9, 14, 10, 0);

        var result = new EffectiveRate(new BigDecimal("0.0125"), validFrom);

        assertThat(result.rate()).isEqualByComparingTo("0.0125");
        assertThat(result.validFrom()).isEqualTo(validFrom);
    }

    @Test
    void shouldAcceptZeroRate() {
        var result = new EffectiveRate(BigDecimal.ZERO, LocalDateTime.of(2026, 9, 14, 10, 0));

        assertThat(result.rate()).isZero();
    }

    @Test
    void shouldRejectNullRate() {
        assertThatThrownBy(() -> new EffectiveRate(null, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Taxa inválida");
    }

    @Test
    void shouldRejectNegativeRate() {
        assertThatThrownBy(() -> new EffectiveRate(new BigDecimal("-0.0001"), LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Taxa inválida");
    }
}
