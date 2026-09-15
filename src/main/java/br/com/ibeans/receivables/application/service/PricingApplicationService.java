package br.com.ibeans.receivables.application.service;

import br.com.ibeans.receivables.application.port.in.pricing.SimulatePricingUseCase;
import br.com.ibeans.receivables.application.port.in.pricing.SimulationCommand;
import br.com.ibeans.receivables.application.port.out.ExchangeRatePort;
import br.com.ibeans.receivables.domain.ExchangeRateQuote;
import br.com.ibeans.receivables.application.port.out.PricingConfigurationPort;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;
import br.com.ibeans.receivables.domain.Receivable;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;
import br.com.ibeans.receivables.domain.pricing.PricingCalculation;
import br.com.ibeans.receivables.domain.pricing.PricingContext;
import br.com.ibeans.receivables.domain.pricing.PricingStrategyRegistry;
import br.com.ibeans.receivables.domain.pricing.TermCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PricingApplicationService implements SimulatePricingUseCase {

    private static final MathContext CALCULATION_CONTEXT =
            new MathContext(34, RoundingMode.HALF_EVEN);

    private final PricingConfigurationPort configurationPort;
    private final ExchangeRatePort exchangeRatePort;
    private final PricingStrategyRegistry strategyRegistry;
    private final TermCalculator termCalculator;
    private final Clock clock;

    @Override
    public PricingCalculation simulate(SimulationCommand command) {
        if (command.maturityDate() == null
                || command.maturityDate().isBefore(java.time.LocalDate.now(clock))) {
            throw new IllegalArgumentException("Data de vencimento não pode estar no passado");
        }

        var referenceDate = command.referenceDate() != null
                ? command.referenceDate()
                : LocalDateTime.now(clock);

        var temporary = new Receivable(
                java.util.UUID.randomUUID(),
                "SIMULATION",
                command.faceValue(),
                command.receivableCurrency(),
                command.acquisitionDate(),
                command.maturityDate(),
                command.type(),
                br.com.ibeans.receivables.domain.ReceivableStatus.PENDING,
                0,
                referenceDate,
                referenceDate
        );

        return calculate(temporary, command.paymentCurrency(), referenceDate);
    }

    public PricingCalculation calculate(
            Receivable receivable,
            Currency paymentCurrency,
            LocalDateTime referenceDate
    ) {
        var term = termCalculator.calculate(
                receivable.acquisitionDate(),
                receivable.maturityDate()
        );

        EffectiveRate baseRate = configurationPort.findBaseRate(
                receivable.currency(),
                referenceDate
        );
        ReceivableTypeConfiguration typeConfiguration =
                configurationPort.findReceivableTypeConfiguration(
                        receivable.type(),
                        referenceDate
                );

        var strategy = strategyRegistry.get(typeConfiguration.strategyKey());
        var pricingResult = strategy.calculate(new PricingContext(
                receivable.faceValue(),
                baseRate.rate(),
                typeConfiguration.spread(),
                term
        ));

        BigDecimal finalAmount = pricingResult.presentValue();
        ExchangeRateQuote exchangeRate = null;

        if (paymentCurrency != receivable.currency()) {
            exchangeRate = exchangeRatePort.find(
                    paymentCurrency,
                    receivable.currency(),
                    referenceDate
            );
            finalAmount = pricingResult.presentValue()
                    .divide(exchangeRate.rate(), CALCULATION_CONTEXT)
                    .setScale(2, RoundingMode.HALF_EVEN);
        }

        return new PricingCalculation(
                term,
                baseRate,
                typeConfiguration,
                strategy.key(),
                strategy.calculationMethod(),
                strategy.calculationVersion(),
                pricingResult.presentValue(),
                receivable.currency(),
                pricingResult.discount(),
                exchangeRate,
                finalAmount,
                paymentCurrency
        );
    }

}
