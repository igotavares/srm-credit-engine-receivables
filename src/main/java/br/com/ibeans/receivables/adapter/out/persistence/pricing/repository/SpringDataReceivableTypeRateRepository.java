package br.com.ibeans.receivables.adapter.out.persistence.pricing.repository;

import br.com.ibeans.receivables.adapter.out.persistence.pricing.entity.ReceivableTypeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataReceivableTypeRateRepository extends JpaRepository<ReceivableTypeRateEntity, UUID> {
    Optional<ReceivableTypeRateEntity> findFirstByTypeKeyAndValidFromLessThanEqualOrderByValidFromDesc(
            String typeKey, LocalDateTime validFrom);
}
