package br.com.ibeans.receivables.application.service;

import br.com.ibeans.receivables.application.exception.BusinessConflictException;
import br.com.ibeans.receivables.application.exception.NotFoundException;
import br.com.ibeans.receivables.application.port.in.settlement.SettleReceivableUseCase;
import br.com.ibeans.receivables.application.port.in.settlement.SettlementResult;
import br.com.ibeans.receivables.application.port.out.*;
import br.com.ibeans.receivables.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementApplicationService implements SettleReceivableUseCase {

    private final ReceivableRepositoryPort receivableRepository;
    private final SettlementRepositoryPort settlementRepository;
    private final IdempotencyLockPort idempotencyLock;
    private final PricingApplicationService pricingService;
    private final Clock clock;

    @Override
    @Transactional
    public SettlementResult settle(
            UUID receivableId,
            Currency paymentCurrency,
            String idempotencyKey
) {
        validateIdempotencyKey(idempotencyKey);

        idempotencyLock.lock(idempotencyKey);

        var previous = settlementRepository.findByIdempotencyKey(idempotencyKey);
        if (previous.isPresent()) {
            var existing = previous.get();
            if (!existing.receivableId().equals(receivableId)
                    || existing.finalCurrency() != paymentCurrency) {
                throw new BusinessConflictException("RECE003", "Conflito",
                        "Idempotency-Key já utilizada por outra requisição"
                );
            }
            return new SettlementResult(existing, true);
        }

        if (settlementRepository.findByReceivableId(receivableId).isPresent()) {
            throw new BusinessConflictException("RECE004",
                    "Conflito",
                    "Recebível já possui liquidação: " + receivableId);
        }

        var receivable = receivableRepository.findById(receivableId)
                .orElseThrow(() -> new NotFoundException("RECE005",
                        "Recebível não encontrado: " + receivableId
                ));

        if (receivable.status() == ReceivableStatus.SETTLED) {
            throw new BusinessConflictException("RECE006",
                    "Recurso não encontrado",
                    "Recebível já liquidado");
        }

        var settledAt = LocalDateTime.now(clock);
        var pricing = pricingService.calculate(receivable, paymentCurrency, settledAt);

        var settlement = new Settlement(
                UUID.randomUUID(),
                receivable.id(),
                receivable.assignorId(),
                receivable.faceValue(),
                pricing.termMonths(),
                pricing.baseRate().rate(),
                pricing.baseRate().validFrom(),
                pricing.typeConfiguration().spread(),
                pricing.typeConfiguration().validFrom(),
                pricing.pricingStrategy(),
                pricing.calculationMethod(),
                pricing.calculationVersion(),
                pricing.presentValue(),
                pricing.presentValueCurrency(),
                pricing.exchangeRate() != null ? pricing.exchangeRate().rate() : null,
                pricing.exchangeRate() != null ? pricing.exchangeRate().validFrom() : null,
                pricing.finalAmount(),
                pricing.finalCurrency(),
                settledAt,
                idempotencyKey
        );

        receivableRepository.save(receivable.settle(settledAt));

        var savedSettlement = settlementRepository.save(settlement);

        return new SettlementResult(savedSettlement, false);
    }

    private void validateIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Header Idempotency-Key é obrigatório");
        }
        if (key.length() > 120) {
            throw new IllegalArgumentException("Idempotency-Key deve possuir no máximo 120 caracteres");
        }
    }

}
