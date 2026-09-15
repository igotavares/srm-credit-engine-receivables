package br.com.ibeans.receivables.application.port.out;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;
import br.com.ibeans.receivables.domain.ReceivableType;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;

import java.time.LocalDateTime;

public interface PricingConfigurationPort {
    EffectiveRate findBaseRate(Currency currency, LocalDateTime at);
    ReceivableTypeConfiguration findReceivableTypeConfiguration(ReceivableType type, LocalDateTime at);
    EffectiveRate saveBaseRate(Currency currency, java.math.BigDecimal rate, LocalDateTime validFrom);
    ReceivableTypeConfiguration saveReceivableTypeConfiguration(
            ReceivableType type,
            java.math.BigDecimal spread,
            String strategyKey,
            LocalDateTime validFrom
    );
}
