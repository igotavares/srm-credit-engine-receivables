package br.com.ibeans.receivables.application.port.out;

import br.com.ibeans.receivables.domain.Receivable;

import java.util.Optional;
import java.util.UUID;

public interface ReceivableRepositoryPort {
    Receivable save(Receivable receivable);
    Optional<Receivable> findById(UUID id);
}
