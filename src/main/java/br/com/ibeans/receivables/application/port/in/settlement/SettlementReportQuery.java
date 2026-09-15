package br.com.ibeans.receivables.application.port.in.settlement;

import br.com.ibeans.receivables.domain.Currency;

import java.time.LocalDateTime;

public record SettlementReportQuery(
        LocalDateTime from,
        LocalDateTime to,
        String assignorId,
        Currency currency,
        int page,
        int size
) {
}
