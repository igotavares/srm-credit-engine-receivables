package br.com.ibeans.receivables.adapter.in.web.receivable.mapper;

import br.com.ibeans.receivables.adapter.in.web.receivable.dto.CreateReceivableRequest;
import br.com.ibeans.receivables.adapter.in.web.receivable.dto.ReceivableResponse;
import br.com.ibeans.receivables.adapter.in.web.receivable.dto.UpdateReceivableRequest;
import br.com.ibeans.receivables.application.port.in.receivable.CreateReceivableCommand;
import br.com.ibeans.receivables.application.port.in.receivable.UpdateReceivableCommand;
import br.com.ibeans.receivables.domain.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReceivableInMapperTest {
    private final ReceivableInMapper mapper = Mappers.getMapper(ReceivableInMapper.class);
    private static final LocalDate ACQUISITION = LocalDate.of(2026, 9, 1);
    private static final LocalDate MATURITY = LocalDate.of(2027, 3, 1);
    private static final LocalDateTime DATE = LocalDateTime.of(2026, 9, 1, 10, 0);

    @Test void shouldMapCreateRequestToCommand() {
        var request = new CreateReceivableRequest("CEDENTE-1", new BigDecimal("1000.50"), Currency.BRL,
                ACQUISITION, MATURITY, ReceivableType.DUPLICATA_MERCANTIL);

        assertThat(mapper.from(request)).isEqualTo(new CreateReceivableCommand(
                "CEDENTE-1", new BigDecimal("1000.50"), Currency.BRL, ACQUISITION, MATURITY,
                ReceivableType.DUPLICATA_MERCANTIL));
    }

    @Test void shouldMapUpdateRequestToCommand() {
        var request = new UpdateReceivableRequest(new BigDecimal("1800.75"), MATURITY,
                ReceivableType.CHEQUE_PRE_DATADO);

        assertThat(mapper.from(request)).isEqualTo(new UpdateReceivableCommand(
                new BigDecimal("1800.75"), MATURITY, ReceivableType.CHEQUE_PRE_DATADO));
    }

    @Test void shouldMapDomainToResponseAndFormatFaceValue() {
        var domain = new Receivable(UUID.randomUUID(), "CEDENTE-1", new BigDecimal("1000.5000"), Currency.USD,
                ACQUISITION, MATURITY, ReceivableType.DUPLICATA_MERCANTIL, ReceivableStatus.SETTLED,
                4, DATE, DATE.plusHours(2));

        var result = mapper.from(domain);

        assertThat(result).isEqualTo(new ReceivableResponse(domain.id(), "CEDENTE-1", "1000.5000",
                Currency.USD, ACQUISITION, MATURITY, ReceivableType.DUPLICATA_MERCANTIL,
                ReceivableStatus.SETTLED, 4, DATE, DATE.plusHours(2)));
    }

    @Test void shouldReturnNullForNullInputs() {
        assertThat(mapper.from((CreateReceivableRequest) null)).isNull();
        assertThat(mapper.from((UpdateReceivableRequest) null)).isNull();
        assertThat(mapper.from((Receivable) null)).isNull();
    }

    @Test void shouldPreserveNullFieldsInCommands() {
        assertThat(mapper.from(new CreateReceivableRequest(null, null, null, null, null, null)))
                .isEqualTo(new CreateReceivableCommand(null, null, null, null, null, null));
        assertThat(mapper.from(new UpdateReceivableRequest(null, null, null)))
                .isEqualTo(new UpdateReceivableCommand(null, null, null));
    }
}
