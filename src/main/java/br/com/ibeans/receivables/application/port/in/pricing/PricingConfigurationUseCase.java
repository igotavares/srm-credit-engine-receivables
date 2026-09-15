package br.com.ibeans.receivables.application.port.in.pricing;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;
import br.com.ibeans.receivables.domain.ReceivableType;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PricingConfigurationUseCase {

    EffectiveRate findBaseRate(Currency currency, LocalDateTime at);

    ReceivableTypeConfiguration findReceivableTypeConfiguration(ReceivableType type, LocalDateTime at);

    EffectiveRate addBaseRate(Currency currency, BigDecimal rate, LocalDateTime validFrom);

    ReceivableTypeConfiguration addTypeRate(
            ReceivableType type,
            BigDecimal spread,
            String strategyKey,
            LocalDateTime validFrom
    );

}
