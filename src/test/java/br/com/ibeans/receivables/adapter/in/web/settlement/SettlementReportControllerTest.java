package br.com.ibeans.receivables.adapter.in.web.settlement;

import br.com.ibeans.receivables.application.port.in.settlement.SettlementReportQuery;
import br.com.ibeans.receivables.application.port.in.settlement.SettlementReportResult;
import br.com.ibeans.receivables.application.port.in.settlement.SettlementReportUseCase;
import br.com.ibeans.receivables.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementReportControllerTest {

    @Mock
    SettlementReportUseCase mockUseCase;

    @InjectMocks
    SettlementReportController controller;

    @Nested
    @DisplayName("When Find")
    class WhenFind {

        @Test
        @DisplayName("Should Return Mapped Report With Filters And Pagination")
        void shouldReturnMappedReportWithFiltersAndPagination() {
            var from = LocalDateTime.of(2026, 9, 1, 0, 0);
            var to = LocalDateTime.of(2026, 9, 14, 23, 59);
            var result = new SettlementReportResult(List.of(item()), 5, 1, 2);
            when(mockUseCase.execute(new SettlementReportQuery(from, to, "CEDENTE-TESTE", Currency.BRL, 1, 2)))
                    .thenReturn(result);

            var response = controller.find(from, to, "CEDENTE-TESTE", Currency.BRL, 1, 2);

            assertThat(response.items()).hasSize(1);
            assertThat(response.items().getFirst().settlementId()).isEqualTo(item().settlementId());
            assertThat(response.items().getFirst().receivableId()).isEqualTo(item().receivableId());
            assertThat(response.items().getFirst().assignorId()).isEqualTo("CEDENTE-TESTE");
            assertThat(response.items().getFirst().finalAmount()).isEqualTo("8363.87");
            assertThat(response.items().getFirst().currency()).isEqualTo(Currency.BRL);
            assertThat(response.items().getFirst().settledAt()).isEqualTo(item().settledAt());
            assertThat(response.total()).isEqualTo(5);
            assertThat(response.page()).isEqualTo(1);
            assertThat(response.size()).isEqualTo(2);

            verify(mockUseCase).execute(argThat(query -> matchesAllFields(query,
                    new SettlementReportQuery(from, to, "CEDENTE-TESTE", Currency.BRL, 1, 2))));
        }

        @Test
        @DisplayName("Should Preserve Null Filters And Return Empty Items")
        void shouldPreserveNullFiltersAndReturnEmptyItems() {
            var result = new SettlementReportResult(List.of(), 0, 0, 20);
            when(mockUseCase.execute(new SettlementReportQuery(null, null, null, null, 0, 20)))
                    .thenReturn(result);

            var response = controller.find(null, null, null, null, 0, 20);

            assertThat(response.items()).isEmpty();
            assertThat(response.total()).isZero();
            assertThat(response.page()).isZero();
            assertThat(response.size()).isEqualTo(20);
            verify(mockUseCase).execute(argThat(query -> matchesAllFields(query,
                    new SettlementReportQuery(null, null, null, null, 0, 20))));
        }

        @Test
        @DisplayName("Should Propagate Use Case Exception")
        void shouldPropagateUseCaseException() {
            var exception = new IllegalStateException("Settlement report unavailable");
            when(mockUseCase.execute(new SettlementReportQuery(null, null, null, null, 0, 20)))
                    .thenThrow(exception);

            assertThatThrownBy(() -> controller.find(null, null, null, null, 0, 20))
                    .isSameAs(exception);

            verify(mockUseCase).execute(argThat(query -> matchesAllFields(query,
                    new SettlementReportQuery(null, null, null, null, 0, 20))));
        }
    }

    boolean matchesAllFields(Object actual, Object expected) {
        assertThat(actual)
                .as("All fields of %s must match", expected.getClass().getSimpleName())
                .usingRecursiveComparison()
                .isEqualTo(expected);
        return true;
    }

    SettlementReportResult.Item item() {
        return new SettlementReportResult.Item(
                UUID.fromString("40000000-0000-0000-0000-000000000001"),
                UUID.fromString("30000000-0000-0000-0000-000000000001"),
                "CEDENTE-TESTE",
                new BigDecimal("8363.87"),
                Currency.BRL,
                LocalDateTime.of(2026, 9, 14, 10, 0)
        );
    }
}
