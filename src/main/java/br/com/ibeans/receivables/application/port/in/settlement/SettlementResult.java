package br.com.ibeans.receivables.application.port.in.settlement;

import br.com.ibeans.receivables.domain.Settlement;

public record SettlementResult(
        Settlement settlement,
        boolean replayed
) {
}
