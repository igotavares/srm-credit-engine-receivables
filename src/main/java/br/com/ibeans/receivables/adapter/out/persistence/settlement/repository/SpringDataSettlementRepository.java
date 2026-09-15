package br.com.ibeans.receivables.adapter.out.persistence.settlement.repository;

import br.com.ibeans.receivables.adapter.out.persistence.settlement.entity.SettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataSettlementRepository extends JpaRepository<SettlementEntity, UUID> {
    Optional<SettlementEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<SettlementEntity> findByReceivableId(UUID receivableId);
}
