package br.com.ibeans.receivables.adapter.out.persistence.receivable;

import br.com.ibeans.receivables.adapter.out.persistence.receivable.entity.ReceivableEntity;
import br.com.ibeans.receivables.adapter.out.persistence.receivable.repository.SpringDataReceivableRepository;
import br.com.ibeans.receivables.application.port.out.ReceivableRepositoryPort;
import br.com.ibeans.receivables.domain.*;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ReceivablePersistenceAdapter implements ReceivableRepositoryPort {

    private final SpringDataReceivableRepository repository;

    ReceivablePersistenceAdapter(SpringDataReceivableRepository repository) {
        this.repository = repository;
    }

    @Override
    public Receivable save(Receivable domain) {
        var entity = repository.findById(domain.id())
                .orElseGet(ReceivableEntity::new);

        if (entity.getId() == null) {
            entity.setId(domain.id());
            entity.setCreatedAt(domain.createdAt());
        }

        entity.setAssignorId(domain.assignorId());
        entity.setFaceValue(domain.faceValue());
        entity.setCurrency(domain.currency().name());
        entity.setAcquisitionDate(domain.acquisitionDate());
        entity.setMaturityDate(domain.maturityDate());
        entity.setType(domain.type().name());
        entity.setStatus(domain.status().name());
        entity.setUpdatedAt(domain.updatedAt());

        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<Receivable> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private Receivable toDomain(ReceivableEntity entity) {
        return new Receivable(
                entity.getId(),
                entity.getAssignorId(),
                entity.getFaceValue(),
                Currency.valueOf(entity.getCurrency()),
                entity.getAcquisitionDate(),
                entity.getMaturityDate(),
                ReceivableType.valueOf(entity.getType()),
                ReceivableStatus.valueOf(entity.getStatus()),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
