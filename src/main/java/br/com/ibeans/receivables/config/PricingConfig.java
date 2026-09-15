package br.com.ibeans.receivables.config;

import br.com.ibeans.receivables.domain.pricing.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PricingConfig {

    @Bean
    public PricingStrategy standardPricingStrategy() {
        return new StandardPricingStrategy();
    }

    @Bean
    public TermCalculator termCalculator() {
        return new CalendarDays30TermCalculator();
    }

    @Bean
    public PricingStrategyRegistry pricingStrategyRegistry(
            List<PricingStrategy> strategies
    ) {
        return new PricingStrategyRegistry(strategies);
    }

}
