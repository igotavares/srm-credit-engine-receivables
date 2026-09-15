package br.com.ibeans.receivables.adapter.out.persistence.pricing.repository;

import br.com.ibeans.receivables.adapter.out.persistence.pricing.entity.BaseRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataBaseRateRepository extends JpaRepository<BaseRateEntity, UUID> {
    Optional<BaseRateEntity> findFirstByCurrencyAndValidFromLessThanEqualOrderByValidFromDesc(
            String currency, LocalDateTime validFrom);
}
