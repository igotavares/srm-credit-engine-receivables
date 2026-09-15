package br.com.ibeans.receivables.application.service;

import br.com.ibeans.receivables.application.port.out.PricingConfigurationPort;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;
import br.com.ibeans.receivables.domain.ReceivableType;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;
import br.com.ibeans.receivables.domain.pricing.PricingStrategy;
import br.com.ibeans.receivables.domain.pricing.PricingStrategyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingConfigurationServiceTest {

    private static final LocalDateTime VALID_FROM = LocalDateTime.of(2026, 9, 14, 10, 0);

    @Mock
    PricingConfigurationPort configurationPort;

    @Mock
    PricingStrategy strategy;

    private PricingStrategyRegistry strategyRegistry;
    private PricingConfigurationService service;

    @BeforeEach
    void setUp() {
        when(strategy.key()).thenReturn("STANDARD");
        strategyRegistry = new PricingStrategyRegistry(List.of(strategy));
        service = new PricingConfigurationService(configurationPort, strategyRegistry);
    }

    @Test
    void shouldDelegateFindBaseRate() {
        var expected = new EffectiveRate(new BigDecimal("0.0125"), VALID_FROM);
        when(configurationPort.findBaseRate(Currency.BRL, VALID_FROM)).thenReturn(expected);

        var result = service.findBaseRate(Currency.BRL, VALID_FROM);

        assertThat(result).isSameAs(expected);
        verify(configurationPort).findBaseRate(Currency.BRL, VALID_FROM);
    }

    @Test
    void shouldDelegateFindReceivableTypeConfiguration() {
        var expected = new ReceivableTypeConfiguration(
                ReceivableType.DUPLICATA_MERCANTIL, new BigDecimal("0.015"), "STANDARD", VALID_FROM);
        when(configurationPort.findReceivableTypeConfiguration(
                ReceivableType.DUPLICATA_MERCANTIL, VALID_FROM)).thenReturn(expected);

        var result = service.findReceivableTypeConfiguration(
                ReceivableType.DUPLICATA_MERCANTIL, VALID_FROM);

        assertThat(result).isSameAs(expected);
        verify(configurationPort).findReceivableTypeConfiguration(
                ReceivableType.DUPLICATA_MERCANTIL, VALID_FROM);
    }

    @Test
    void shouldSaveBaseRateWhenRateIsZero() {
        var expected = new EffectiveRate(BigDecimal.ZERO, VALID_FROM);
        when(configurationPort.saveBaseRate(Currency.USD, BigDecimal.ZERO, VALID_FROM)).thenReturn(expected);

        var result = service.addBaseRate(Currency.USD, BigDecimal.ZERO, VALID_FROM);

        assertThat(result).isSameAs(expected);
        verify(configurationPort).saveBaseRate(Currency.USD, BigDecimal.ZERO, VALID_FROM);
    }

    @Test
    void shouldRejectNullBaseRate() {
        assertThatThrownBy(() -> service.addBaseRate(Currency.BRL, null, VALID_FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Taxa base deve ser maior ou igual a zero");
        verifyNoInteractions(configurationPort);
    }

    @Test
    void shouldRejectNegativeBaseRate() {
        assertThatThrownBy(() -> service.addBaseRate(Currency.BRL, new BigDecimal("-0.01"), VALID_FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Taxa base deve ser maior ou igual a zero");
        verifyNoInteractions(configurationPort);
    }

    @Test
    void shouldValidateStrategyAndSaveTypeRate() {
        var expected = new ReceivableTypeConfiguration(
                ReceivableType.CHEQUE_PRE_DATADO, BigDecimal.ZERO, "STANDARD", VALID_FROM);
        when(configurationPort.saveReceivableTypeConfiguration(
                ReceivableType.CHEQUE_PRE_DATADO, BigDecimal.ZERO, "STANDARD", VALID_FROM))
                .thenReturn(expected);

        var result = service.addTypeRate(
                ReceivableType.CHEQUE_PRE_DATADO, BigDecimal.ZERO, "STANDARD", VALID_FROM);

        assertThat(result).isSameAs(expected);
        verify(configurationPort).saveReceivableTypeConfiguration(
                ReceivableType.CHEQUE_PRE_DATADO, BigDecimal.ZERO, "STANDARD", VALID_FROM);
    }

    @Test
    void shouldRejectNullSpread() {
        assertThatThrownBy(() -> service.addTypeRate(
                ReceivableType.DUPLICATA_MERCANTIL, null, "STANDARD", VALID_FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Spread deve ser maior ou igual a zero");
        verifyNoInteractions(configurationPort);
    }

    @Test
    void shouldRejectNegativeSpread() {
        assertThatThrownBy(() -> service.addTypeRate(
                ReceivableType.DUPLICATA_MERCANTIL, new BigDecimal("-0.01"), "STANDARD", VALID_FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Spread deve ser maior ou igual a zero");
        verifyNoInteractions(configurationPort);
    }

    @Test
    void shouldRejectUnknownStrategyBeforeSaving() {
        assertThatThrownBy(() -> service.addTypeRate(
                ReceivableType.DUPLICATA_MERCANTIL, BigDecimal.ZERO, "UNKNOWN", VALID_FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Strategy de precificação não encontrada: UNKNOWN");
        verifyNoInteractions(configurationPort);
    }
}
