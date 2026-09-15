package br.com.ibeans.receivables.domain.pricing;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PricingStrategyRegistry {

    private final Map<String, PricingStrategy> strategies;

    public PricingStrategyRegistry(List<PricingStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(PricingStrategy::key, Function.identity()));
    }

    public PricingStrategy get(String key) {
        var strategy = strategies.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy de precificação não encontrada: " + key);
        }
        return strategy;
    }
}
