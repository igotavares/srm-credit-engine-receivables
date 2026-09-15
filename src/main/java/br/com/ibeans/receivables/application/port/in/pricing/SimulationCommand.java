package br.com.ibeans.receivables.application.port.in.pricing;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.ReceivableType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SimulationCommand(
        BigDecimal faceValue,
        Currency receivableCurrency,
        LocalDate acquisitionDate,
        LocalDate maturityDate,
        ReceivableType type,
        Currency paymentCurrency,
        LocalDateTime referenceDate
) {
}
