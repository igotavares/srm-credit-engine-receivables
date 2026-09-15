package br.com.ibeans.receivables.adapter.out.persistence.settlement;

import br.com.ibeans.receivables.adapter.out.persistence.settlement.entity.SettlementEntity;
import br.com.ibeans.receivables.adapter.out.persistence.settlement.repository.SpringDataSettlementRepository;
import br.com.ibeans.receivables.application.port.out.SettlementRepositoryPort;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.Settlement;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SettlementPersistenceAdapter implements SettlementRepositoryPort {

    private final SpringDataSettlementRepository repository;

    SettlementPersistenceAdapter(SpringDataSettlementRepository repository) {
        this.repository = repository;
    }

    @Override
    public Settlement save(Settlement domain) {
        var entity = new SettlementEntity();
        entity.setId(domain.id());
        entity.setReceivableId(domain.receivableId());
        entity.setAssignorId(domain.assignorId());
        entity.setFaceValue(domain.faceValue());
        entity.setTermMonths(domain.termMonths());
        entity.setBaseRate(domain.baseRate());
        entity.setBaseRateReferenceDate(domain.baseRateReferenceDate());
        entity.setSpread(domain.spread());
        entity.setSpreadReferenceDate(domain.spreadReferenceDate());
        entity.setPricingStrategy(domain.pricingStrategy());
        entity.setCalculationMethod(domain.calculationMethod());
        entity.setCalculationVersion(domain.calculationVersion());
        entity.setPresentValue(domain.presentValue());
        entity.setPresentValueCurrency(domain.presentValueCurrency().name());
        entity.setExchangeRate(domain.exchangeRate());
        entity.setExchangeRateReferenceDate(domain.exchangeRateReferenceDate());
        entity.setFinalAmount(domain.finalAmount());
        entity.setFinalCurrency(domain.finalCurrency().name());
        entity.setSettledAt(domain.settledAt());
        entity.setIdempotencyKey(domain.idempotencyKey());

        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<Settlement> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    @Override
    public Optional<Settlement> findByReceivableId(UUID receivableId) {
        return repository.findByReceivableId(receivableId).map(this::toDomain);
    }

    private Settlement toDomain(SettlementEntity entity) {
        return new Settlement(
                entity.getId(),
                entity.getReceivableId(),
                entity.getAssignorId(),
                entity.getFaceValue(),
                entity.getTermMonths(),
                entity.getBaseRate(),
                entity.getBaseRateReferenceDate(),
                entity.getSpread(),
                entity.getSpreadReferenceDate(),
                entity.getPricingStrategy(),
                entity.getCalculationMethod(),
                entity.getCalculationVersion(),
                entity.getPresentValue(),
                Currency.valueOf(entity.getPresentValueCurrency()),
                entity.getExchangeRate(),
                entity.getExchangeRateReferenceDate(),
                entity.getFinalAmount(),
                Currency.valueOf(entity.getFinalCurrency()),
                entity.getSettledAt(),
                entity.getIdempotencyKey()
        );
    }
}
