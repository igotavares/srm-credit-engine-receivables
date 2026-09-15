package br.com.ibeans.receivables.application.service;

import br.com.ibeans.receivables.application.exception.NotFoundException;
import br.com.ibeans.receivables.application.port.in.receivable.CreateReceivableCommand;
import br.com.ibeans.receivables.application.port.in.receivable.UpdateReceivableCommand;
import br.com.ibeans.receivables.application.port.out.ReceivableRepositoryPort;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.Receivable;
import br.com.ibeans.receivables.domain.ReceivableStatus;
import br.com.ibeans.receivables.domain.ReceivableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceivableApplicationServiceTest {

    private static final UUID ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 14, 13, 0);
    private static final LocalDate TODAY = NOW.toLocalDate();

    @Mock
    ReceivableRepositoryPort repository;

    private ReceivableApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ReceivableApplicationService(
                repository, Clock.fixed(Instant.parse("2026-09-14T13:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void shouldCreatePendingReceivableWithGeneratedIdAndCurrentTimestamps() {
        var command = new CreateReceivableCommand(
                "CEDENTE-1", new BigDecimal("1000.00"), Currency.BRL,
                TODAY, TODAY.plusDays(30), ReceivableType.DUPLICATA_MERCANTIL);
        var saved = receivable(ID, command.faceValue(), command.maturityDate(), ReceivableStatus.PENDING, 0);
        when(repository.save(org.mockito.ArgumentMatchers.any(Receivable.class))).thenReturn(saved);

        var result = service.create(command);

        assertThat(result).isSameAs(saved);
        var captor = ArgumentCaptor.forClass(Receivable.class);
        verify(repository).save(captor.capture());
        var created = captor.getValue();
        assertThat(created.id()).isNotNull();
        assertThat(created.assignorId()).isEqualTo("CEDENTE-1");
        assertThat(created.faceValue()).isEqualByComparingTo("1000.00");
        assertThat(created.currency()).isEqualTo(Currency.BRL);
        assertThat(created.status()).isEqualTo(ReceivableStatus.PENDING);
        assertThat(created.version()).isZero();
        assertThat(created.createdAt()).isEqualTo(NOW);
        assertThat(created.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldRejectNullMaturityWhenCreating() {
        var command = new CreateReceivableCommand("CEDENTE-1", new BigDecimal("1000"), Currency.BRL,
                TODAY, null, ReceivableType.DUPLICATA_MERCANTIL);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de vencimento não pode estar no passado");
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectPastMaturityWhenUpdating() {
        var command = new UpdateReceivableCommand(new BigDecimal("1200"), TODAY.minusDays(1),
                ReceivableType.CHEQUE_PRE_DATADO);

        assertThatThrownBy(() -> service.update(ID, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de vencimento não pode estar no passado");
        verifyNoInteractions(repository);
    }

    @Test
    void shouldFindReceivableById() {
        var expected = receivable(ID, new BigDecimal("1000"), TODAY.plusDays(30), ReceivableStatus.PENDING, 2);
        when(repository.findById(ID)).thenReturn(Optional.of(expected));

        assertThat(service.find(ID)).isSameAs(expected);
        verify(repository).findById(ID);
    }

    @Test
    void shouldThrowNotFoundWhenReceivableDoesNotExist() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Recebível não encontrado: " + ID);
        verify(repository).findById(ID);
    }

    @Test
    void shouldUpdateReceivableAndPreserveItsIdentityAndCreationData() {
        var current = receivable(ID, new BigDecimal("1000"), TODAY.plusDays(30), ReceivableStatus.PENDING, 3);
        var command = new UpdateReceivableCommand(new BigDecimal("1500.75"), TODAY.plusDays(60),
                ReceivableType.CHEQUE_PRE_DATADO);
        var updated = receivable(ID, command.faceValue(), command.maturityDate(), current.status(), current.version());
        when(repository.findById(ID)).thenReturn(Optional.of(current));
        when(repository.save(org.mockito.ArgumentMatchers.any(Receivable.class))).thenReturn(updated);

        var result = service.update(ID, command);

        assertThat(result).isSameAs(updated);
        var captor = ArgumentCaptor.forClass(Receivable.class);
        verify(repository).save(captor.capture());
        var actual = captor.getValue();
        assertThat(actual.id()).isEqualTo(ID);
        assertThat(actual.faceValue()).isEqualByComparingTo("1500.75");
        assertThat(actual.maturityDate()).isEqualTo(TODAY.plusDays(60));
        assertThat(actual.type()).isEqualTo(ReceivableType.CHEQUE_PRE_DATADO);
        assertThat(actual.createdAt()).isEqualTo(current.createdAt());
        assertThat(actual.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldPropagateDomainErrorWhenUpdatingSettledReceivable() {
        var settled = receivable(ID, new BigDecimal("1000"), TODAY.plusDays(30), ReceivableStatus.SETTLED, 1);
        when(repository.findById(ID)).thenReturn(Optional.of(settled));

        assertThatThrownBy(() -> service.update(ID,
                new UpdateReceivableCommand(new BigDecimal("1100"), TODAY.plusDays(40),
                        ReceivableType.DUPLICATA_MERCANTIL)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Recebível liquidado é imutável");
        verify(repository).findById(ID);
    }

    private Receivable receivable(UUID id, BigDecimal faceValue, LocalDate maturityDate,
                                  ReceivableStatus status, long version) {
        return new Receivable(id, "CEDENTE-1", faceValue, Currency.BRL, TODAY, maturityDate,
                ReceivableType.DUPLICATA_MERCANTIL, status, version, NOW.minusDays(1), NOW.minusDays(1));
    }
}
