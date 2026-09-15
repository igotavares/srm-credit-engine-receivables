package br.com.ibeans.receivables.application.port.out;

import br.com.ibeans.receivables.domain.Settlement;

import java.util.Optional;
import java.util.UUID;

public interface SettlementRepositoryPort {
    Settlement save(Settlement settlement);
    Optional<Settlement> findByIdempotencyKey(String idempotencyKey);
    Optional<Settlement> findByReceivableId(UUID receivableId);
}
