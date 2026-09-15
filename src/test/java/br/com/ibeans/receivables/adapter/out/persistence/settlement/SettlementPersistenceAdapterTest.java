package br.com.ibeans.receivables.adapter.out.persistence.settlement;

import br.com.ibeans.receivables.adapter.out.persistence.settlement.entity.SettlementEntity;
import br.com.ibeans.receivables.adapter.out.persistence.settlement.repository.SpringDataSettlementRepository;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.Settlement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementPersistenceAdapterTest {
    private static final UUID SETTLEMENT_ID = UUID.randomUUID();
    private static final UUID RECEIVABLE_ID = UUID.randomUUID();
    private static final LocalDateTime DATE = LocalDateTime.of(2026, 9, 14, 10, 0);
    @Mock SpringDataSettlementRepository repository;
    private SettlementPersistenceAdapter adapter;

    @BeforeEach void setUp() { adapter = new SettlementPersistenceAdapter(repository); }

    @Test void shouldMapDomainToEntityAndPersistAndMapResultBack() {
        var domain = settlement(Currency.USD);
        var persisted = entity(Currency.USD);
        when(repository.saveAndFlush(any(SettlementEntity.class))).thenReturn(persisted);

        var result = adapter.save(domain);

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
        var captor = ArgumentCaptor.forClass(SettlementEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        var actual = captor.getValue();
        assertThat(actual.getId()).isEqualTo(domain.id());
        assertThat(actual.getReceivableId()).isEqualTo(domain.receivableId());
        assertThat(actual.getAssignorId()).isEqualTo(domain.assignorId());
        assertThat(actual.getPresentValueCurrency()).isEqualTo("BRL");
        assertThat(actual.getFinalCurrency()).isEqualTo("USD");
        assertThat(actual.getExchangeRate()).isEqualByComparingTo(domain.exchangeRate());
        assertThat(actual.getIdempotencyKey()).isEqualTo(domain.idempotencyKey());
    }

    @Test void shouldFindByIdempotencyKeyAndMapEntity() {
        var entity = entity(Currency.BRL);
        when(repository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(entity));

        var result = adapter.findByIdempotencyKey("key-1");

        assertThat(result).isPresent();
        assertThat(result.get()).usingRecursiveComparison().isEqualTo(settlement(Currency.BRL));
        verify(repository).findByIdempotencyKey("key-1");
    }

    @Test void shouldReturnEmptyWhenIdempotencyKeyIsNotFound() {
        when(repository.findByIdempotencyKey("missing")).thenReturn(Optional.empty());

        assertThat(adapter.findByIdempotencyKey("missing")).isEmpty();
        verify(repository).findByIdempotencyKey("missing");
    }

    @Test void shouldFindByReceivableIdAndReturnEmptyWhenNotFound() {
        when(repository.findByReceivableId(RECEIVABLE_ID)).thenReturn(Optional.empty());

        assertThat(adapter.findByReceivableId(RECEIVABLE_ID)).isEmpty();
        verify(repository).findByReceivableId(RECEIVABLE_ID);
    }

    private Settlement settlement(Currency finalCurrency) {
        return new Settlement(SETTLEMENT_ID, RECEIVABLE_ID, "CEDENTE-1", new BigDecimal("1000"),
                new BigDecimal("1"), new BigDecimal(".01"), DATE, new BigDecimal(".015"), DATE,
                "STANDARD", "method", 1L, new BigDecimal("970"), Currency.BRL,
                finalCurrency == Currency.USD ? new BigDecimal("5.4321") : null,
                finalCurrency == Currency.USD ? DATE : null, new BigDecimal("970"), finalCurrency, DATE, "key-1");
    }

    private SettlementEntity entity(Currency finalCurrency) {
        var entity = new SettlementEntity();
        entity.setId(SETTLEMENT_ID); entity.setReceivableId(RECEIVABLE_ID); entity.setAssignorId("CEDENTE-1");
        entity.setFaceValue(new BigDecimal("1000")); entity.setTermMonths(new BigDecimal("1"));
        entity.setBaseRate(new BigDecimal(".01")); entity.setBaseRateReferenceDate(DATE);
        entity.setSpread(new BigDecimal(".015")); entity.setSpreadReferenceDate(DATE);
        entity.setPricingStrategy("STANDARD"); entity.setCalculationMethod("method"); entity.setCalculationVersion(1L);
        entity.setPresentValue(new BigDecimal("970")); entity.setPresentValueCurrency("BRL");
        entity.setExchangeRate(finalCurrency == Currency.USD ? new BigDecimal("5.4321") : null);
        entity.setExchangeRateReferenceDate(finalCurrency == Currency.USD ? DATE : null);
        entity.setFinalAmount(new BigDecimal("970")); entity.setFinalCurrency(finalCurrency.name());
        entity.setSettledAt(DATE); entity.setIdempotencyKey("key-1");
        return entity;
    }
}
