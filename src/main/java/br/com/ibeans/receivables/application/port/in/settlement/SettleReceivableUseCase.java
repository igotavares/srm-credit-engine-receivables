package br.com.ibeans.receivables.application.port.in.settlement;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.Settlement;

import java.util.UUID;

public interface SettleReceivableUseCase {

    SettlementResult settle(UUID receivableId, Currency paymentCurrency, String idempotencyKey);

}
