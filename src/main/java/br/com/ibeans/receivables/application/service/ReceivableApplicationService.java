package br.com.ibeans.receivables.application.service;

import br.com.ibeans.receivables.application.port.in.receivable.CreateReceivableCommand;
import br.com.ibeans.receivables.application.port.in.receivable.UpdateReceivableCommand;
import br.com.ibeans.receivables.application.exception.NotFoundException;
import br.com.ibeans.receivables.application.port.in.receivable.ReceivableUseCases;
import br.com.ibeans.receivables.application.port.out.ReceivableRepositoryPort;
import br.com.ibeans.receivables.domain.Receivable;
import br.com.ibeans.receivables.domain.ReceivableStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceivableApplicationService implements ReceivableUseCases {

    private final ReceivableRepositoryPort repository;
    private final Clock clock;

    @Override
    @Transactional
    public Receivable create(CreateReceivableCommand command) {
        validateMaturity(command.maturityDate());

        var now = LocalDateTime.now(clock);
        var receivable = new Receivable(
                UUID.randomUUID(),
                command.assignorId(),
                command.faceValue(),
                command.currency(),
                command.acquisitionDate(),
                command.maturityDate(),
                command.type(),
                ReceivableStatus.PENDING,
                0,
                now,
                now
        );

        return repository.save(receivable);
    }

    @Override
    @Transactional
    public Receivable update(UUID id, UpdateReceivableCommand command) {
        validateMaturity(command.maturityDate());

        var current = find(id);
        var now = LocalDateTime.now(clock);
        var updated = current.update(
                command.faceValue(),
                command.maturityDate(),
                command.type(),
                now
        );

        return repository.save(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Receivable find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("RECE007",
                        "Recebível não encontrado: " + id));
    }

    private void validateMaturity(LocalDate maturityDate) {
        if (maturityDate == null || maturityDate.isBefore(LocalDate.now(clock))) {
            throw new IllegalArgumentException("Data de vencimento não pode estar no passado");
        }
    }

}
