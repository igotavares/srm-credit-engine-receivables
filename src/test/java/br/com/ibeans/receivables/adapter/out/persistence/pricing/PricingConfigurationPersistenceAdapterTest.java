package br.com.ibeans.receivables.adapter.out.persistence.pricing;

import br.com.ibeans.receivables.adapter.out.persistence.pricing.entity.BaseRateEntity;
import br.com.ibeans.receivables.adapter.out.persistence.pricing.entity.ReceivableTypeRateEntity;
import br.com.ibeans.receivables.adapter.out.persistence.pricing.repository.SpringDataBaseRateRepository;
import br.com.ibeans.receivables.adapter.out.persistence.pricing.repository.SpringDataReceivableTypeRateRepository;
import br.com.ibeans.receivables.application.exception.NotFoundException;
import br.com.ibeans.receivables.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingConfigurationPersistenceAdapterTest {
    private static final LocalDateTime DATE = LocalDateTime.of(2026, 9, 14, 10, 0);
    @Mock SpringDataBaseRateRepository baseRepository;
    @Mock SpringDataReceivableTypeRateRepository typeRepository;
    private PricingConfigurationPersistenceAdapter adapter;

    @BeforeEach void setUp() { adapter = new PricingConfigurationPersistenceAdapter(baseRepository, typeRepository); }

    @Test void shouldFindBaseRateAndMapIt() {
        var entity = new BaseRateEntity(); entity.setCurrency("USD"); entity.setRate(new BigDecimal(".0125")); entity.setValidFrom(DATE);
        when(baseRepository.findFirstByCurrencyAndValidFromLessThanEqualOrderByValidFromDesc("USD", DATE)).thenReturn(Optional.of(entity));

        var result = adapter.findBaseRate(Currency.USD, DATE);

        assertThat(result.rate()).isEqualByComparingTo(".0125"); assertThat(result.validFrom()).isEqualTo(DATE);
        verify(baseRepository).findFirstByCurrencyAndValidFromLessThanEqualOrderByValidFromDesc("USD", DATE);
    }

    @Test void shouldThrowWhenBaseRateIsNotFound() {
        when(baseRepository.findFirstByCurrencyAndValidFromLessThanEqualOrderByValidFromDesc("BRL", DATE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.findBaseRate(Currency.BRL, DATE)).isInstanceOf(NotFoundException.class)
                .hasMessage("Taxa base não encontrada para BRL em " + DATE);
    }

    @Test void shouldFindTypeConfigurationAndMapIt() {
        var entity = new ReceivableTypeRateEntity(); entity.setSpread(new BigDecimal(".015")); entity.setStrategyKey("STANDARD"); entity.setValidFrom(DATE);
        when(typeRepository.findFirstByTypeKeyAndValidFromLessThanEqualOrderByValidFromDesc("DUPLICATA_MERCANTIL", DATE)).thenReturn(Optional.of(entity));

        var result = adapter.findReceivableTypeConfiguration(ReceivableType.DUPLICATA_MERCANTIL, DATE);

        assertThat(result.type()).isEqualTo(ReceivableType.DUPLICATA_MERCANTIL); assertThat(result.spread()).isEqualByComparingTo(".015");
        assertThat(result.strategyKey()).isEqualTo("STANDARD"); assertThat(result.validFrom()).isEqualTo(DATE);
    }

    @Test void shouldThrowWhenTypeConfigurationIsNotFound() {
        when(typeRepository.findFirstByTypeKeyAndValidFromLessThanEqualOrderByValidFromDesc("CHEQUE_PRE_DATADO", DATE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.findReceivableTypeConfiguration(ReceivableType.CHEQUE_PRE_DATADO, DATE))
                .isInstanceOf(NotFoundException.class).hasMessage("Configuração do tipo CHEQUE_PRE_DATADO não encontrada em " + DATE);
    }

    @Test void shouldSaveBaseRateWithCurrencyAndReturnSavedValues() {
        var saved = new BaseRateEntity(); saved.setRate(new BigDecimal(".01")); saved.setValidFrom(DATE);
        when(baseRepository.save(any(BaseRateEntity.class))).thenReturn(saved);

        var result = adapter.saveBaseRate(Currency.BRL, new BigDecimal(".02"), DATE);

        assertThat(result.rate()).isEqualByComparingTo(".01"); assertThat(result.validFrom()).isEqualTo(DATE);
        var captor = ArgumentCaptor.forClass(BaseRateEntity.class); verify(baseRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNotNull(); assertThat(captor.getValue().getCurrency()).isEqualTo("BRL");
        assertThat(captor.getValue().getRate()).isEqualByComparingTo(".02");
    }

    @Test void shouldSaveTypeRateWithTypeAndReturnSavedValues() {
        var saved = new ReceivableTypeRateEntity(); saved.setSpread(new BigDecimal(".03")); saved.setStrategyKey("STANDARD"); saved.setValidFrom(DATE);
        when(typeRepository.save(any(ReceivableTypeRateEntity.class))).thenReturn(saved);

        var result = adapter.saveReceivableTypeConfiguration(ReceivableType.CHEQUE_PRE_DATADO, new BigDecimal(".02"), "OTHER", DATE);

        assertThat(result.type()).isEqualTo(ReceivableType.CHEQUE_PRE_DATADO); assertThat(result.spread()).isEqualByComparingTo(".03");
        assertThat(result.strategyKey()).isEqualTo("STANDARD");
        var captor = ArgumentCaptor.forClass(ReceivableTypeRateEntity.class); verify(typeRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNotNull(); assertThat(captor.getValue().getTypeKey()).isEqualTo("CHEQUE_PRE_DATADO");
        assertThat(captor.getValue().getSpread()).isEqualByComparingTo(".02"); assertThat(captor.getValue().getStrategyKey()).isEqualTo("OTHER");
    }
}
