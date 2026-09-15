package br.com.ibeans.receivables.application.port.in.receivable;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.ReceivableType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateReceivableCommand(
        String assignorId,
        BigDecimal faceValue,
        Currency currency,
        LocalDate acquisitionDate,
        LocalDate maturityDate,
        ReceivableType type
) {
}
