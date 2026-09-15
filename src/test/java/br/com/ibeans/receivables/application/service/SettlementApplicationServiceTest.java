package br.com.ibeans.receivables.application.service;

import br.com.ibeans.receivables.application.exception.BusinessConflictException;
import br.com.ibeans.receivables.application.exception.NotFoundException;
import br.com.ibeans.receivables.application.port.out.IdempotencyLockPort;
import br.com.ibeans.receivables.application.port.out.ReceivableRepositoryPort;
import br.com.ibeans.receivables.application.port.out.SettlementRepositoryPort;
import br.com.ibeans.receivables.domain.*;
import br.com.ibeans.receivables.domain.pricing.PricingCalculation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementApplicationServiceTest {
    private static final UUID ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final String KEY = "settlement-001";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 14, 13, 0);
    @Mock ReceivableRepositoryPort receivableRepository;
    @Mock SettlementRepositoryPort settlementRepository;
    @Mock IdempotencyLockPort idempotencyLock;
    @Mock PricingApplicationService pricingService;
    private SettlementApplicationService service;

    @BeforeEach void setUp() {
        service = new SettlementApplicationService(receivableRepository, settlementRepository,
                idempotencyLock, pricingService, Clock.fixed(Instant.parse("2026-09-14T13:00:00Z"), ZoneOffset.UTC));
    }

    @Test void shouldRejectInvalidIdempotencyKeyBeforeLocking() {
        for (String key : new String[]{null, "", "   ", "x".repeat(121)}) {
            assertThatThrownBy(() -> service.settle(ID, Currency.BRL, key))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        verifyNoInteractions(idempotencyLock, settlementRepository, receivableRepository, pricingService);
    }

    @Test void shouldReplayPreviousSettlementWhenRequestMatches() {
        var existing = settlement(Currency.BRL);
        when(settlementRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.of(existing));

        var result = service.settle(ID, Currency.BRL, KEY);

        assertThat(result.settlement()).isSameAs(existing);
        assertThat(result.replayed()).isTrue();
        verify(idempotencyLock).lock(KEY);
        verifyNoInteractions(receivableRepository, pricingService);
    }

    @Test void shouldRejectIdempotencyConflict() {
        when(settlementRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.of(settlement(Currency.BRL)));

        assertThatThrownBy(() -> service.settle(UUID.randomUUID(), Currency.BRL, KEY))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("Idempotency-Key já utilizada por outra requisição");
        verifyNoInteractions(receivableRepository, pricingService);
    }

    @Test void shouldRejectReceivableWithExistingSettlement() {
        when(settlementRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.empty());
        when(settlementRepository.findByReceivableId(ID)).thenReturn(Optional.of(settlement(Currency.BRL)));

        assertThatThrownBy(() -> service.settle(ID, Currency.BRL, KEY))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("Recebível já possui liquidação: " + ID);
        verifyNoInteractions(receivableRepository, pricingService);
    }

    @Test void shouldRejectMissingReceivable() {
        when(settlementRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.empty());
        when(settlementRepository.findByReceivableId(ID)).thenReturn(Optional.empty());
        when(receivableRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.settle(ID, Currency.BRL, KEY))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Recebível não encontrado: " + ID);
        verifyNoInteractions(pricingService);
    }

    @Test void shouldRejectAlreadySettledReceivable() {
        when(settlementRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.empty());
        when(settlementRepository.findByReceivableId(ID)).thenReturn(Optional.empty());
        when(receivableRepository.findById(ID)).thenReturn(Optional.of(receivable(ReceivableStatus.SETTLED)));

        assertThatThrownBy(() -> service.settle(ID, Currency.BRL, KEY))
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage("Recebível já liquidado");
        verifyNoInteractions(pricingService);
    }

    @Test void shouldCalculateAndSaveSettlementAndMarkReceivableAsSettled() {
        var receivable = receivable(ReceivableStatus.PENDING);
        var calculation = calculation(Currency.BRL);
        var saved = settlement(Currency.BRL);
        when(settlementRepository.findByIdempotencyKey(KEY)).thenReturn(Optional.empty());
        when(settlementRepository.findByReceivableId(ID)).thenReturn(Optional.empty());
        when(receivableRepository.findById(ID)).thenReturn(Optional.of(receivable));
        when(pricingService.calculate(receivable, Currency.BRL, NOW)).thenReturn(calculation);
        when(settlementRepository.save(any())).thenReturn(saved);

        var result = service.settle(ID, Currency.BRL, KEY);

        assertThat(result.settlement()).isSameAs(saved);
        assertThat(result.replayed()).isFalse();
        verify(pricingService).calculate(receivable, Currency.BRL, NOW);
        var settledCaptor = ArgumentCaptor.forClass(Receivable.class);
        verify(receivableRepository).save(settledCaptor.capture());
        assertThat(settledCaptor.getValue().status()).isEqualTo(ReceivableStatus.SETTLED);
        verify(settlementRepository).save(any(Settlement.class));
    }

    private Receivable receivable(ReceivableStatus status) {
        return new Receivable(ID, "CEDENTE-1", new BigDecimal("1000"), Currency.BRL,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1), ReceivableType.DUPLICATA_MERCANTIL,
                status, 0, NOW.minusDays(1), NOW.minusDays(1));
    }

    private Settlement settlement(Currency currency) {
        return new Settlement(ID, ID, "CEDENTE-1", new BigDecimal("1000"), BigDecimal.ONE,
                new BigDecimal(".01"), NOW, new BigDecimal(".015"), NOW, "STANDARD", "method", 1L,
                new BigDecimal("970"), Currency.BRL, null, null, new BigDecimal("970"), currency, NOW, KEY);
    }

    private PricingCalculation calculation(Currency currency) {
        return new PricingCalculation(BigDecimal.ONE, new EffectiveRate(new BigDecimal(".01"), NOW),
                new ReceivableTypeConfiguration(ReceivableType.DUPLICATA_MERCANTIL, new BigDecimal(".015"), "STANDARD", NOW),
                "STANDARD", "method", 1, new BigDecimal("970"), Currency.BRL, new BigDecimal("30"),
                null, new BigDecimal("970"), currency);
    }
}
