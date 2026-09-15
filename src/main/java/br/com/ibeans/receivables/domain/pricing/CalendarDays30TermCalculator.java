package br.com.ibeans.receivables.domain.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CalendarDays30TermCalculator implements TermCalculator {

    private static final BigDecimal DAYS_PER_MONTH = new BigDecimal("30");

    @Override
    public BigDecimal calculate(LocalDate acquisitionDate, LocalDate maturityDate) {
        long days = ChronoUnit.DAYS.between(acquisitionDate, maturityDate);
        if (days <= 0) {
            throw new IllegalArgumentException("Vencimento deve ser posterior à aquisição");
        }
        return BigDecimal.valueOf(days)
                .divide(DAYS_PER_MONTH, 12, RoundingMode.HALF_EVEN)
                .stripTrailingZeros();
    }
}
