package br.com.ibeans.receivables.application.port.in.receivable;

import br.com.ibeans.receivables.domain.ReceivableType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateReceivableCommand(
        BigDecimal faceValue,
        LocalDate maturityDate,
        ReceivableType type
) {
}
