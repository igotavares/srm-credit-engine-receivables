package br.com.ibeans.receivables.adapter.out.persistence.pricing;

import br.com.ibeans.receivables.adapter.out.persistence.pricing.entity.BaseRateEntity;
import br.com.ibeans.receivables.adapter.out.persistence.pricing.entity.ReceivableTypeRateEntity;
import br.com.ibeans.receivables.adapter.out.persistence.pricing.repository.SpringDataBaseRateRepository;
import br.com.ibeans.receivables.adapter.out.persistence.pricing.repository.SpringDataReceivableTypeRateRepository;
import br.com.ibeans.receivables.application.exception.NotFoundException;
import br.com.ibeans.receivables.application.port.out.PricingConfigurationPort;
import br.com.ibeans.receivables.domain.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
class PricingConfigurationPersistenceAdapter implements PricingConfigurationPort {

    private final SpringDataBaseRateRepository baseRateRepository;
    private final SpringDataReceivableTypeRateRepository typeRateRepository;

    PricingConfigurationPersistenceAdapter(
            SpringDataBaseRateRepository baseRateRepository,
            SpringDataReceivableTypeRateRepository typeRateRepository
    ) {
        this.baseRateRepository = baseRateRepository;
        this.typeRateRepository = typeRateRepository;
    }

    @Override
    public EffectiveRate findBaseRate(Currency currency, LocalDateTime at) {
        return baseRateRepository
                .findFirstByCurrencyAndValidFromLessThanEqualOrderByValidFromDesc(currency.name(), at)
                .map(entity -> new EffectiveRate(entity.getRate(), entity.getValidFrom()))
                .orElseThrow(() -> new NotFoundException(
                        "RECE001",
                        "Taxa base não encontrada para " + currency + " em " + at
                ));
    }

    @Override
    public ReceivableTypeConfiguration findReceivableTypeConfiguration(
            ReceivableType type,
            LocalDateTime at
    ) {
        return typeRateRepository
                .findFirstByTypeKeyAndValidFromLessThanEqualOrderByValidFromDesc(type.name(), at)
                .map(entity -> new ReceivableTypeConfiguration(
                        type,
                        entity.getSpread(),
                        entity.getStrategyKey(),
                        entity.getValidFrom()
                ))
                .orElseThrow(() -> new NotFoundException(
                        "RECE002",
                        "Configuração do tipo " + type + " não encontrada em " + at
                ));
    }

    @Override
    public EffectiveRate saveBaseRate(
            Currency currency,
            BigDecimal rate,
            LocalDateTime validFrom
    ) {
        var entity = new BaseRateEntity();
        entity.setId(UUID.randomUUID());
        entity.setCurrency(currency.name());
        entity.setRate(rate);
        entity.setValidFrom(validFrom);
        var saved = baseRateRepository.save(entity);
        return new EffectiveRate(saved.getRate(), saved.getValidFrom());
    }

    @Override
    public ReceivableTypeConfiguration saveReceivableTypeConfiguration(
            ReceivableType type,
            BigDecimal spread,
            String strategyKey,
            LocalDateTime validFrom
    ) {
        var entity = new ReceivableTypeRateEntity();
        entity.setId(UUID.randomUUID());
        entity.setTypeKey(type.name());
        entity.setSpread(spread);
        entity.setStrategyKey(strategyKey);
        entity.setValidFrom(validFrom);
        var saved = typeRateRepository.save(entity);
        return new ReceivableTypeConfiguration(
                type,
                saved.getSpread(),
                saved.getStrategyKey(),
                saved.getValidFrom()
        );
    }
}
