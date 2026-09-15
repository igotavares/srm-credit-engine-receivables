package br.com.ibeans.receivables.application.port.in.settlement;

import br.com.ibeans.receivables.domain.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SettlementReportResult(
        List<Item> items,
        long total,
        int page,
        int size
) {
    public record Item(
            UUID settlementId,
            UUID receivableId,
            String assignorId,
            BigDecimal finalAmount,
            Currency currency,
            LocalDateTime settledAt
    ) {
    }
}
