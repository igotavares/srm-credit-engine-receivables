package br.com.ibeans.receivables.application.service;

import br.com.ibeans.receivables.application.port.in.pricing.PricingConfigurationUseCase;
import br.com.ibeans.receivables.application.port.out.PricingConfigurationPort;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;
import br.com.ibeans.receivables.domain.ReceivableType;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;
import br.com.ibeans.receivables.domain.pricing.PricingStrategyRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PricingConfigurationService implements PricingConfigurationUseCase {

    private final PricingConfigurationPort configurationPort;
    private final PricingStrategyRegistry strategyRegistry;

    public PricingConfigurationService(
            PricingConfigurationPort configurationPort,
            PricingStrategyRegistry strategyRegistry
    ) {
        this.configurationPort = configurationPort;
        this.strategyRegistry = strategyRegistry;
    }

    @Override
    @Transactional(readOnly = true)
    public EffectiveRate findBaseRate(Currency currency, LocalDateTime at) {
        return configurationPort.findBaseRate(currency, at);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceivableTypeConfiguration findReceivableTypeConfiguration(ReceivableType type, LocalDateTime at) {
        return configurationPort.findReceivableTypeConfiguration(type, at);
    }

    @Override
    @Transactional
    public EffectiveRate addBaseRate(Currency currency, BigDecimal rate, LocalDateTime validFrom) {
        if (rate == null || rate.signum() < 0) {
            throw new IllegalArgumentException("Taxa base deve ser maior ou igual a zero");
        }
        return configurationPort.saveBaseRate(currency, rate, validFrom);
    }

    @Override
    @Transactional
    public ReceivableTypeConfiguration addTypeRate(
            ReceivableType type,
            BigDecimal spread,
            String strategyKey,
            LocalDateTime validFrom
    ) {
        strategyRegistry.get(strategyKey);
        if (spread == null || spread.signum() < 0) {
            throw new IllegalArgumentException("Spread deve ser maior ou igual a zero");
        }
        return configurationPort.saveReceivableTypeConfiguration(type, spread, strategyKey, validFrom);
    }
}
