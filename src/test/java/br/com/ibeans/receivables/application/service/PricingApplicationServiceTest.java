package br.com.ibeans.receivables.application.service;

import br.com.ibeans.receivables.application.port.in.pricing.SimulationCommand;
import br.com.ibeans.receivables.application.port.out.ExchangeRatePort;
import br.com.ibeans.receivables.domain.ExchangeRateQuote;
import br.com.ibeans.receivables.application.port.out.PricingConfigurationPort;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;
import br.com.ibeans.receivables.domain.ReceivableType;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;
import br.com.ibeans.receivables.domain.pricing.CalendarDays30TermCalculator;
import br.com.ibeans.receivables.domain.pricing.PricingStrategyRegistry;
import br.com.ibeans.receivables.domain.pricing.StandardPricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PricingApplicationServiceTest {

    private PricingConfigurationPort configuration;
    private ExchangeRatePort exchangeRate;
    private PricingApplicationService service;

    @BeforeEach
    void setUp() {
        configuration = mock(PricingConfigurationPort.class);
        exchangeRate = mock(ExchangeRatePort.class);

        when(configuration.findBaseRate(any(), any()))
                .thenReturn(new EffectiveRate(
                        new BigDecimal("0.01"),
                        LocalDateTime.of(2026, 1, 1, 0, 0)
                ));

        when(configuration.findReceivableTypeConfiguration(eq(ReceivableType.DUPLICATA_MERCANTIL), any()))
                .thenReturn(new ReceivableTypeConfiguration(
                        ReceivableType.DUPLICATA_MERCANTIL,
                        new BigDecimal("0.015"),
                        "STANDARD",
                        LocalDateTime.of(2026, 1, 1, 0, 0)
                ));

        when(configuration.findReceivableTypeConfiguration(eq(ReceivableType.CHEQUE_PRE_DATADO), any()))
                .thenReturn(new ReceivableTypeConfiguration(
                        ReceivableType.CHEQUE_PRE_DATADO,
                        new BigDecimal("0.025"),
                        "STANDARD",
                        LocalDateTime.of(2026, 1, 1, 0, 0)
                ));

        when(exchangeRate.find(eq(Currency.USD), eq(Currency.BRL), any()))
                .thenReturn(new ExchangeRateQuote(
                        new BigDecimal("5.4321"),
                        LocalDateTime.of(2026, 2, 1, 12, 0)
                ));

        var registry = new PricingStrategyRegistry(List.of(new StandardPricingStrategy()));
        service = new PricingApplicationService(
                configuration,
                exchangeRate,
                registry,
                new CalendarDays30TermCalculator(),
                Clock.fixed(Instant.parse("2026-02-01T15:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void c1DuplicataBRL() {
        var result = service.simulate(command(
                "100000.00",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1),
                ReceivableType.DUPLICATA_MERCANTIL,
                Currency.BRL
        ));

        assertThat(result.presentValue()).isEqualByComparingTo("92859.94");
        assertThat(result.finalAmount()).isEqualByComparingTo("92859.94");
        assertThat(result.discount()).isEqualByComparingTo("7140.06");
    }

    @Test
    void c2ChequeBRL() {
        var result = service.simulate(command(
                "25000.00",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 2),
                ReceivableType.CHEQUE_PRE_DATADO,
                Currency.BRL
        ));

        assertThat(result.presentValue()).isEqualByComparingTo("23337.77");
        assertThat(result.finalAmount()).isEqualByComparingTo("23337.77");
        assertThat(result.discount()).isEqualByComparingTo("1662.23");
    }

    @Test
    void c3DuplicataUSD() {
        var result = service.simulate(command(
                "100000.00",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1),
                ReceivableType.DUPLICATA_MERCANTIL,
                Currency.USD
        ));

        assertThat(result.presentValue()).isEqualByComparingTo("92859.94");
        assertThat(result.exchangeRate().rate()).isEqualByComparingTo("5.4321");
        assertThat(result.finalAmount()).isEqualByComparingTo("17094.67");
    }

    @Test
    void shouldRejectNullMaturityDate() {
        var command = command("1000", LocalDate.of(2026, 1, 1), null,
                ReceivableType.DUPLICATA_MERCANTIL, Currency.BRL);

        assertThatThrownBy(() -> service.simulate(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de vencimento não pode estar no passado");
    }

    @Test
    void shouldRejectPastMaturityDate() {
        var command = command("1000", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                ReceivableType.DUPLICATA_MERCANTIL, Currency.BRL);

        assertThatThrownBy(() -> service.simulate(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de vencimento não pode estar no passado");
    }

    @Test
    void shouldUseClockAsReferenceDateWhenCommandReferenceDateIsNull() {
        var command = new SimulationCommand(
                new BigDecimal("1000"), Currency.BRL, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1), ReceivableType.DUPLICATA_MERCANTIL, Currency.BRL, null);

        var result = service.simulate(command);

        assertThat(result.exchangeRate()).isNull();
        verify(configuration).findBaseRate(Currency.BRL, LocalDateTime.of(2026, 2, 1, 15, 0));
        verify(configuration).findReceivableTypeConfiguration(
                ReceivableType.DUPLICATA_MERCANTIL, LocalDateTime.of(2026, 2, 1, 15, 0));
    }

    @Test
    void shouldKeepFinalAmountInReceivableCurrencyWithoutExchangeRate() {
        var result = service.calculate(
                new br.com.ibeans.receivables.domain.Receivable(
                        java.util.UUID.randomUUID(), "CEDENTE", new BigDecimal("1000"), Currency.BRL,
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1),
                        ReceivableType.DUPLICATA_MERCANTIL,
                        br.com.ibeans.receivables.domain.ReceivableStatus.PENDING, 0,
                        LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0)),
                Currency.BRL, LocalDateTime.of(2026, 2, 1, 12, 0));

        assertThat(result.exchangeRate()).isNull();
        assertThat(result.finalCurrency()).isEqualTo(Currency.BRL);
        assertThat(result.finalAmount()).isEqualByComparingTo(result.presentValue());
    }

    private SimulationCommand command(
            String faceValue,
            LocalDate acquisition,
            LocalDate maturity,
            ReceivableType type,
            Currency paymentCurrency
    ) {
        return new SimulationCommand(
                new BigDecimal(faceValue),
                Currency.BRL,
                acquisition,
                maturity,
                type,
                paymentCurrency,
                LocalDateTime.of(2026, 2, 1, 12, 0)
        );
    }
}
