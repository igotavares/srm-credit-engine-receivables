package br.com.ibeans.receivables.application.port.out;

import br.com.ibeans.receivables.domain.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SettlementReportPort {

    ReportPage find(
            LocalDateTime from,
            LocalDateTime to,
            String assignorId,
            Currency currency,
            int page,
            int size
    );

    record ReportItem(
            UUID settlementId,
            UUID receivableId,
            String assignorId,
            BigDecimal finalAmount,
            Currency currency,
            LocalDateTime settledAt
    ) {
    }

    record ReportPage(
            List<ReportItem> items,
            long total,
            int page,
            int size
    ) {
    }
}
