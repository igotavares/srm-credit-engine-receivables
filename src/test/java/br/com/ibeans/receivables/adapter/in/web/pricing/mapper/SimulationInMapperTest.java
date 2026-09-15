package br.com.ibeans.receivables.adapter.in.web.pricing.mapper;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.SimulationRequest;
import br.com.ibeans.receivables.application.port.in.pricing.SimulationCommand;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.ReceivableType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationInMapperTest {

    private final SimulationInMapper mapper = Mappers.getMapper(SimulationInMapper.class);
    private static final LocalDate ACQUISITION = LocalDate.of(2026, 9, 1);
    private static final LocalDate MATURITY = LocalDate.of(2027, 3, 15);
    private static final LocalDateTime REFERENCE = LocalDateTime.of(2026, 9, 15, 10, 30);

    @Test
    void shouldMapAllFieldsPreservingPrecisionAndDistinctCurrencies() {
        var request = new SimulationRequest(new BigDecimal("12345.678900"), Currency.BRL,
                ACQUISITION, MATURITY, ReceivableType.DUPLICATA_MERCANTIL, Currency.USD, REFERENCE);

        assertThat(mapper.from(request)).isEqualTo(new SimulationCommand(
                new BigDecimal("12345.678900"), Currency.BRL, ACQUISITION, MATURITY,
                ReceivableType.DUPLICATA_MERCANTIL, Currency.USD, REFERENCE));
    }

    @Test
    void shouldPreserveMissingOptionalReferenceDate() {
        var request = new SimulationRequest(new BigDecimal("500.00"), Currency.USD,
                ACQUISITION, MATURITY, ReceivableType.CHEQUE_PRE_DATADO, Currency.BRL, null);

        assertThat(mapper.from(request)).isEqualTo(new SimulationCommand(
                new BigDecimal("500.00"), Currency.USD, ACQUISITION, MATURITY,
                ReceivableType.CHEQUE_PRE_DATADO, Currency.BRL, null));
    }

    @Test
    void shouldReturnNullForNullRequest() {
        assertThat(mapper.from(null)).isNull();
    }

    @Test
    void shouldPreserveNullFields() {
        var request = new SimulationRequest(null, null, null, null, null, null, null);

        assertThat(mapper.from(request))
                .isEqualTo(new SimulationCommand(null, null, null, null, null, null, null));
    }
}
