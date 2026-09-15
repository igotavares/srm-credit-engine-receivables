package br.com.ibeans.receivables.adapter.out.persistence.receivable;

import br.com.ibeans.receivables.adapter.out.persistence.receivable.entity.ReceivableEntity;
import br.com.ibeans.receivables.adapter.out.persistence.receivable.repository.SpringDataReceivableRepository;
import br.com.ibeans.receivables.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceivablePersistenceAdapterTest {
    private static final UUID ID = UUID.randomUUID();
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 9, 1, 10, 0);
    @Mock SpringDataReceivableRepository repository;
    private ReceivablePersistenceAdapter adapter;

    @BeforeEach void setUp() { adapter = new ReceivablePersistenceAdapter(repository); }

    @Test void shouldCreateEntityAndMapPersistedEntityToDomain() {
        var domain = receivable(ReceivableStatus.PENDING, 0);
        var persisted = entity(ReceivableStatus.PENDING, 0);
        when(repository.saveAndFlush(any(ReceivableEntity.class))).thenReturn(persisted);

        var result = adapter.save(domain);

        assertThat(result).usingRecursiveComparison().isEqualTo(domain);
        var captor = ArgumentCaptor.forClass(ReceivableEntity.class);
        verify(repository).findById(ID);
        verify(repository).saveAndFlush(captor.capture());
        var actual = captor.getValue();
        assertThat(actual.getId()).isEqualTo(ID);
        assertThat(actual.getCurrency()).isEqualTo("BRL");
        assertThat(actual.getType()).isEqualTo("DUPLICATA_MERCANTIL");
        assertThat(actual.getStatus()).isEqualTo("PENDING");
        assertThat(actual.getCreatedAt()).isEqualTo(CREATED);
    }

    @Test void shouldUpdateExistingEntityWithoutReplacingCreatedAtOrVersion() {
        var current = entity(ReceivableStatus.PENDING, 4);
        var domain = receivable(ReceivableStatus.SETTLED, 4);
        domain = new Receivable(ID, "CEDENTE-UPDATED", new BigDecimal("1500"), Currency.USD,
                domain.acquisitionDate(), domain.maturityDate(), ReceivableType.CHEQUE_PRE_DATADO,
                ReceivableStatus.SETTLED, 4, CREATED.minusDays(5), CREATED.plusDays(1));
        when(repository.findById(ID)).thenReturn(Optional.of(current));
        when(repository.saveAndFlush(any(ReceivableEntity.class))).thenReturn(entity(ReceivableStatus.SETTLED, 4));

        adapter.save(domain);

        var captor = ArgumentCaptor.forClass(ReceivableEntity.class);
        verify(repository).saveAndFlush(captor.capture());
        var actual = captor.getValue();
        assertThat(actual.getId()).isEqualTo(ID);
        assertThat(actual.getAssignorId()).isEqualTo("CEDENTE-UPDATED");
        assertThat(actual.getFaceValue()).isEqualByComparingTo("1500");
        assertThat(actual.getCurrency()).isEqualTo("USD");
        assertThat(actual.getType()).isEqualTo("CHEQUE_PRE_DATADO");
        assertThat(actual.getStatus()).isEqualTo("SETTLED");
        assertThat(actual.getCreatedAt()).isEqualTo(current.getCreatedAt());
        assertThat(actual.getVersion()).isEqualTo(current.getVersion());
        assertThat(actual.getUpdatedAt()).isEqualTo(domain.updatedAt());
    }

    @Test void shouldFindAndMapEntityById() {
        when(repository.findById(ID)).thenReturn(Optional.of(entity(ReceivableStatus.PENDING, 2)));

        var result = adapter.findById(ID);

        assertThat(result).isPresent();
        assertThat(result.get()).usingRecursiveComparison().isEqualTo(receivable(ReceivableStatus.PENDING, 2));
        verify(repository).findById(ID);
    }

    @Test void shouldReturnEmptyWhenIdIsNotFound() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThat(adapter.findById(ID)).isEmpty();
        verify(repository).findById(ID);
    }

    private Receivable receivable(ReceivableStatus status, long version) {
        return new Receivable(ID, "CEDENTE-1", new BigDecimal("1000"), Currency.BRL,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1),
                ReceivableType.DUPLICATA_MERCANTIL, status, version, CREATED, CREATED);
    }

    private ReceivableEntity entity(ReceivableStatus status, long version) {
        var entity = new ReceivableEntity();
        entity.setId(ID); entity.setAssignorId("CEDENTE-1"); entity.setFaceValue(new BigDecimal("1000"));
        entity.setCurrency("BRL"); entity.setAcquisitionDate(LocalDate.of(2026, 9, 1));
        entity.setMaturityDate(LocalDate.of(2026, 10, 1)); entity.setType("DUPLICATA_MERCANTIL");
        entity.setStatus(status.name()); entity.setVersion(version); entity.setCreatedAt(CREATED); entity.setUpdatedAt(CREATED);
        return entity;
    }
}
