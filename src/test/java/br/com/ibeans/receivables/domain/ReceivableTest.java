package br.com.ibeans.receivables.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReceivableTest {
    private static final UUID ID = UUID.randomUUID();
    private static final LocalDate ACQUISITION = LocalDate.of(2026, 9, 1);
    private static final LocalDate MATURITY = LocalDate.of(2026, 10, 1);
    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 9, 1, 10, 0);

    @Test
    void shouldCreateValidReceivable() {
        var result = receivable();

        assertThat(result.id()).isEqualTo(ID);
        assertThat(result.assignorId()).isEqualTo("CEDENTE-1");
        assertThat(result.faceValue()).isEqualByComparingTo("1000");
        assertThat(result.status()).isEqualTo(ReceivableStatus.PENDING);
    }

    @Test
    void shouldRejectMissingRequiredFields() {
        assertThatThrownBy(() -> new Receivable(null, "CEDENTE-1", new BigDecimal("1000"), Currency.BRL,
                ACQUISITION, MATURITY, ReceivableType.DUPLICATA_MERCANTIL, ReceivableStatus.PENDING, 0, CREATED, CREATED))
                .hasMessage("Id do recebível é obrigatório");
        assertThatThrownBy(() -> new Receivable(ID, " ", new BigDecimal("1000"), Currency.BRL,
                ACQUISITION, MATURITY, ReceivableType.DUPLICATA_MERCANTIL, ReceivableStatus.PENDING, 0, CREATED, CREATED))
                .hasMessage("Cedente é obrigatório");
        assertThatThrownBy(() -> new Receivable(ID, "CEDENTE-1", null, Currency.BRL,
                ACQUISITION, MATURITY, ReceivableType.DUPLICATA_MERCANTIL, ReceivableStatus.PENDING, 0, CREATED, CREATED))
                .hasMessage("Valor de face deve ser maior que zero");
    }

    @Test
    void shouldRejectInvalidDatesAndEnums() {
        assertThatThrownBy(() -> new Receivable(ID, "CEDENTE-1", new BigDecimal("1000"), Currency.BRL,
                ACQUISITION, ACQUISITION, ReceivableType.DUPLICATA_MERCANTIL, ReceivableStatus.PENDING, 0, CREATED, CREATED))
                .hasMessage("Vencimento deve ser posterior à aquisição");
        assertThatThrownBy(() -> new Receivable(ID, "CEDENTE-1", new BigDecimal("1000"), null,
                ACQUISITION, MATURITY, ReceivableType.DUPLICATA_MERCANTIL, ReceivableStatus.PENDING, 0, CREATED, CREATED))
                .hasMessage("Moeda do título é obrigatória");
        assertThatThrownBy(() -> new Receivable(ID, "CEDENTE-1", new BigDecimal("1000"), Currency.BRL,
                null, MATURITY, ReceivableType.DUPLICATA_MERCANTIL, ReceivableStatus.PENDING, 0, CREATED, CREATED))
                .hasMessage("Datas são obrigatórias");
    }

    @Test
    void shouldUpdatePendingReceivable() {
        var when = CREATED.plusHours(2);

        var result = receivable().update(new BigDecimal("1500.75"), LocalDate.of(2026, 11, 1),
                ReceivableType.CHEQUE_PRE_DATADO, when);

        assertThat(result.id()).isEqualTo(ID);
        assertThat(result.assignorId()).isEqualTo("CEDENTE-1");
        assertThat(result.faceValue()).isEqualByComparingTo("1500.75");
        assertThat(result.maturityDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(result.type()).isEqualTo(ReceivableType.CHEQUE_PRE_DATADO);
        assertThat(result.status()).isEqualTo(ReceivableStatus.PENDING);
        assertThat(result.createdAt()).isEqualTo(CREATED);
        assertThat(result.updatedAt()).isEqualTo(when);
    }

    @Test
    void shouldSettlePendingReceivable() {
        var settledAt = CREATED.plusDays(1);

        var result = receivable().settle(settledAt);

        assertThat(result.status()).isEqualTo(ReceivableStatus.SETTLED);
        assertThat(result.id()).isEqualTo(ID);
        assertThat(result.faceValue()).isEqualByComparingTo("1000");
        assertThat(result.updatedAt()).isEqualTo(settledAt);
        assertThat(result.createdAt()).isEqualTo(CREATED);
    }

    @Test
    void shouldRejectUpdateOfSettledReceivable() {
        assertThatThrownBy(() -> receivable(ReceivableStatus.SETTLED).update(
                new BigDecimal("1200"), MATURITY.plusDays(1), ReceivableType.DUPLICATA_MERCANTIL, CREATED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Recebível liquidado é imutável");
    }

    @Test
    void shouldRejectSettlementOfAlreadySettledReceivable() {
        assertThatThrownBy(() -> receivable(ReceivableStatus.SETTLED).settle(CREATED.plusDays(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Recebível já liquidado");
    }

    private Receivable receivable() { return receivable(ReceivableStatus.PENDING); }

    private Receivable receivable(ReceivableStatus status) {
        return new Receivable(ID, "CEDENTE-1", new BigDecimal("1000"), Currency.BRL,
                ACQUISITION, MATURITY, ReceivableType.DUPLICATA_MERCANTIL, status, 2, CREATED, CREATED);
    }
}
