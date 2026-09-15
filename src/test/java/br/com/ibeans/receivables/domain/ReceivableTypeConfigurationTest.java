package br.com.ibeans.receivables.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReceivableTypeConfigurationTest {
    private static final ReceivableType TYPE = ReceivableType.DUPLICATA_MERCANTIL;
    private static final LocalDateTime VALID_FROM = LocalDateTime.of(2026, 9, 1, 10, 0);

    @Test
    void shouldCreateValidConfiguration() {
        var result = new ReceivableTypeConfiguration(TYPE, new BigDecimal("0.015"), "STANDARD", VALID_FROM);

        assertThat(result.type()).isEqualTo(TYPE);
        assertThat(result.spread()).isEqualByComparingTo("0.015");
        assertThat(result.strategyKey()).isEqualTo("STANDARD");
        assertThat(result.validFrom()).isEqualTo(VALID_FROM);
    }

    @Test
    void shouldAllowZeroSpread() {
        var result = new ReceivableTypeConfiguration(TYPE, BigDecimal.ZERO, "STANDARD", VALID_FROM);

        assertThat(result.spread()).isZero();
    }

    @Test
    void shouldAllowStrategyWithSurroundingSpaces() {
        var result = new ReceivableTypeConfiguration(TYPE, BigDecimal.ONE, " STANDARD ", VALID_FROM);

        assertThat(result.strategyKey()).isEqualTo(" STANDARD ");
    }

    @Test
    void shouldRejectNullSpread() {
        assertThatThrownBy(() -> new ReceivableTypeConfiguration(TYPE, null, "STANDARD", VALID_FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Spread inválido");
    }

    @Test
    void shouldRejectNegativeSpread() {
        assertThatThrownBy(() -> new ReceivableTypeConfiguration(TYPE, new BigDecimal("-0.001"), "STANDARD", VALID_FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Spread inválido");
    }

    @Test
    void shouldRejectNullStrategy() {
        assertThatThrownBy(() -> new ReceivableTypeConfiguration(TYPE, BigDecimal.ZERO, null, VALID_FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Strategy é obrigatória");
    }

    @Test
    void shouldRejectBlankStrategy() {
        assertThatThrownBy(() -> new ReceivableTypeConfiguration(TYPE, BigDecimal.ZERO, " \t\n", VALID_FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Strategy é obrigatória");
    }
}
