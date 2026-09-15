package br.com.ibeans.receivables.adapter.in.web.settlement;

import br.com.ibeans.receivables.adapter.in.web.settlement.dto.SettlementRequest;
import br.com.ibeans.receivables.adapter.in.web.settlement.dto.SettlementResponse;
import br.com.ibeans.receivables.application.port.in.settlement.SettleReceivableUseCase;
import br.com.ibeans.receivables.application.port.in.settlement.SettlementResult;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.Settlement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementControllerTest {

    private static final UUID RECEIVABLE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final String IDEMPOTENCY_KEY = "settlement-test";

    @Mock
    SettleReceivableUseCase mockUseCase;

    @InjectMocks
    SettlementController controller;

    @Nested
    @DisplayName("When Settle")
    class WhenSettle {

        @Test
        @DisplayName("Should Return Created Response For New Settlement")
        void shouldReturnCreatedResponseForNewSettlement() {
            var result = new SettlementResult(settlement(), false);
            when(mockUseCase.settle(RECEIVABLE_ID, Currency.BRL, IDEMPOTENCY_KEY)).thenReturn(result);

            var response = controller.settle(RECEIVABLE_ID, IDEMPOTENCY_KEY, request());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getHeaders().getFirst("Idempotency-Replayed")).isEqualTo("false");
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(settlementResponse());
            verify(mockUseCase).settle(RECEIVABLE_ID, Currency.BRL, IDEMPOTENCY_KEY);
        }

        @Test
        @DisplayName("Should Return Ok Response For Replayed Settlement")
        void shouldReturnOkResponseForReplayedSettlement() {
            var result = new SettlementResult(settlement(), true);
            when(mockUseCase.settle(RECEIVABLE_ID, Currency.BRL, IDEMPOTENCY_KEY)).thenReturn(result);

            var response = controller.settle(RECEIVABLE_ID, IDEMPOTENCY_KEY, request());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getFirst("Idempotency-Replayed")).isEqualTo("true");
            assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(settlementResponse());
            verify(mockUseCase).settle(RECEIVABLE_ID, Currency.BRL, IDEMPOTENCY_KEY);
        }

        @Test
        @DisplayName("Should Propagate Use Case Exception")
        void shouldPropagateUseCaseException() {
            var exception = new IllegalStateException("Receivable cannot be settled");
            when(mockUseCase.settle(RECEIVABLE_ID, Currency.BRL, IDEMPOTENCY_KEY)).thenThrow(exception);

            assertThatThrownBy(() -> controller.settle(RECEIVABLE_ID, IDEMPOTENCY_KEY, request()))
                    .isSameAs(exception);

            verify(mockUseCase).settle(RECEIVABLE_ID, Currency.BRL, IDEMPOTENCY_KEY);
        }
    }

    SettlementRequest request() {
        return new SettlementRequest(Currency.BRL);
    }

    Settlement settlement() {
        return new Settlement(
                UUID.fromString("40000000-0000-0000-0000-000000000001"),
                RECEIVABLE_ID,
                "CEDENTE-TESTE",
                new BigDecimal("10000.00"),
                new BigDecimal("12"),
                new BigDecimal("0.01"),
                LocalDateTime.of(2026, 9, 14, 8, 0),
                new BigDecimal("0.005"),
                LocalDateTime.of(2026, 9, 14, 9, 0),
                "STANDARD",
                "PV=FV/(1+BASE_RATE+SPREAD)^TERM",
                1L,
                new BigDecimal("8363.87"),
                Currency.BRL,
                null,
                null,
                new BigDecimal("8363.87"),
                Currency.BRL,
                LocalDateTime.of(2026, 9, 14, 10, 0),
                IDEMPOTENCY_KEY
        );
    }

    SettlementResponse settlementResponse() {
        return SettlementResponse.from(settlement());
    }
}
