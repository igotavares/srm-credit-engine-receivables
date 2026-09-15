package br.com.ibeans.receivables.domain.pricing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingStrategyRegistryTest {

    @Test
    void shouldReturnStrategyRegisteredWithTheRequestedKey() {
        var strategy = strategy("STANDARD");
        var registry = new PricingStrategyRegistry(List.of(strategy));

        assertThat(registry.get("STANDARD")).isSameAs(strategy);
    }

    @Test
    void shouldRejectUnknownStrategyKey() {
        var registry = new PricingStrategyRegistry(List.of(strategy("STANDARD")));

        assertThatThrownBy(() -> registry.get("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Strategy de precificação não encontrada: UNKNOWN");
    }

    @Test
    void shouldRejectStrategyKeyThatWasNotRegisteredInAnEmptyRegistry() {
        var registry = new PricingStrategyRegistry(List.of());

        assertThatThrownBy(() -> registry.get("STANDARD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Strategy de precificação não encontrada: STANDARD");
    }

    @Test
    void shouldRejectDuplicateStrategyKeys() {
        var first = strategy("STANDARD");
        var second = strategy("STANDARD");

        assertThatThrownBy(() -> new PricingStrategyRegistry(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class);
    }

    private PricingStrategy strategy(String key) {
        return new PricingStrategy() {
            @Override public String key() { return key; }
            @Override public String calculationMethod() { return "method"; }
            @Override public long calculationVersion() { return 1; }
            @Override public PricingResult calculate(PricingContext context) { return null; }
        };
    }
}
