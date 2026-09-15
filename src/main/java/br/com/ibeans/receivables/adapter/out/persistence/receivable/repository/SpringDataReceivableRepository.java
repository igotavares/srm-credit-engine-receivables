package br.com.ibeans.receivables.adapter.out.persistence.receivable.repository;

import br.com.ibeans.receivables.adapter.out.persistence.receivable.entity.ReceivableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataReceivableRepository extends JpaRepository<ReceivableEntity, UUID> {
}
