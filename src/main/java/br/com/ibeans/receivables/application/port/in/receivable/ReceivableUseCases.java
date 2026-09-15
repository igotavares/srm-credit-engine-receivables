package br.com.ibeans.receivables.application.port.in.receivable;

import br.com.ibeans.receivables.domain.Receivable;

import java.util.UUID;

public interface ReceivableUseCases {

    Receivable create(CreateReceivableCommand command);

    Receivable update(UUID id, UpdateReceivableCommand command);

    Receivable find(UUID id);

}
