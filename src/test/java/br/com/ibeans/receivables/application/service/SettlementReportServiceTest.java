package br.com.ibeans.receivables.application.service;

import br.com.ibeans.receivables.application.port.in.settlement.SettlementReportQuery;
import br.com.ibeans.receivables.application.port.out.SettlementReportPort;
import br.com.ibeans.receivables.domain.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementReportServiceTest {
    private static final LocalDateTime FROM = LocalDateTime.of(2026, 9, 1, 0, 0);
    private static final LocalDateTime TO = LocalDateTime.of(2026, 9, 14, 23, 59);
    @Mock SettlementReportPort reportPort;
    private SettlementReportService service;

    @BeforeEach void setUp() { service = new SettlementReportService(reportPort); }

    @Test void shouldDelegateQueryAndMapReportItemsAndMetadata() {
        var query = new SettlementReportQuery(FROM, TO, "CEDENTE-1", Currency.BRL, 1, 20);
        var first = new SettlementReportPort.ReportItem(UUID.randomUUID(), UUID.randomUUID(), "CEDENTE-1",
                new BigDecimal("970.50"), Currency.BRL, TO);
        var second = new SettlementReportPort.ReportItem(UUID.randomUUID(), UUID.randomUUID(), "CEDENTE-2",
                new BigDecimal("1200.00"), Currency.USD, FROM);
        when(reportPort.find(FROM, TO, "CEDENTE-1", Currency.BRL, 1, 20))
                .thenReturn(new SettlementReportPort.ReportPage(List.of(first, second), 42, 1, 20));

        var result = service.execute(query);

        assertThat(result.total()).isEqualTo(42);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.items()).hasSize(2)
                .extracting("settlementId", "receivableId", "assignorId", "finalAmount", "currency", "settledAt")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(first.settlementId(), first.receivableId(), first.assignorId(), first.finalAmount(), first.currency(), first.settledAt()),
                        org.assertj.core.groups.Tuple.tuple(second.settlementId(), second.receivableId(), second.assignorId(), second.finalAmount(), second.currency(), second.settledAt()));
        verify(reportPort).find(FROM, TO, "CEDENTE-1", Currency.BRL, 1, 20);
    }

    @Test void shouldReturnEmptyItemsAndPreservePageMetadata() {
        var query = new SettlementReportQuery(null, null, null, null, 0, 200);
        when(reportPort.find(null, null, null, null, 0, 200))
                .thenReturn(new SettlementReportPort.ReportPage(List.of(), 0, 0, 200));

        var result = service.execute(query);

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(200);
        verify(reportPort).find(null, null, null, null, 0, 200);
    }

    @Test void shouldRejectNegativePage() {
        assertThatThrownBy(() -> service.execute(new SettlementReportQuery(null, null, null, null, -1, 20)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("page deve ser >= 0");
        verifyNoInteractions(reportPort);
    }

    @Test void shouldRejectSizeOutsideAllowedRange() {
        for (int size : new int[]{0, 201}) {
            assertThatThrownBy(() -> service.execute(new SettlementReportQuery(null, null, null, null, 0, size)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("size deve estar entre 1 e 200");
        }
        verifyNoInteractions(reportPort);
    }

    @Test void shouldPropagateReportPortException() {
        var query = new SettlementReportQuery(FROM, TO, null, Currency.USD, 0, 10);
        var exception = new IllegalStateException("report unavailable");
        when(reportPort.find(FROM, TO, null, Currency.USD, 0, 10)).thenThrow(exception);

        assertThatThrownBy(() -> service.execute(query)).isSameAs(exception);
        verify(reportPort).find(FROM, TO, null, Currency.USD, 0, 10);
    }
}
