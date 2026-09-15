package br.com.ibeans.receivables.domain.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TermCalculator {

    BigDecimal calculate(LocalDate acquisitionDate, LocalDate maturityDate);

}
