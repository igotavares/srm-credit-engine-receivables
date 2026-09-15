package br.com.ibeans.receivables.domain.pricing;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarDays30TermCalculatorTest {

    private final CalendarDays30TermCalculator calculator = new CalendarDays30TermCalculator();

    @Test
    void deveCalcular30DiasComoUmMes() {
        assertThat(calculator.calculate(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        )).isEqualByComparingTo("1");
    }

    @Test
    void deveCalcular45DiasComoUmMesEMeio() {
        assertThat(calculator.calculate(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 15)
        )).isEqualByComparingTo("1.5");
    }

    @Test
    void deveCalcular90DiasComoTresMeses() {
        assertThat(calculator.calculate(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 1)
        )).isEqualByComparingTo("3");
    }
}
