package br.com.ibeans.receivables.adapter.in.web.settlement;

import br.com.ibeans.receivables.adapter.exception.ServiceUnavailableException;
import br.com.ibeans.receivables.adapter.in.web.AbstractControllerIT;
import br.com.ibeans.receivables.adapter.out.persistence.pricing.entity.BaseRateEntity;
import br.com.ibeans.receivables.adapter.out.persistence.pricing.entity.ReceivableTypeRateEntity;
import br.com.ibeans.receivables.adapter.out.persistence.receivable.entity.ReceivableEntity;
import br.com.ibeans.receivables.application.port.out.ExchangeRatePort;
import br.com.ibeans.receivables.domain.ExchangeRateQuote;
import br.com.ibeans.receivables.domain.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SettlementControllerIntegrationIT extends AbstractControllerIT {

    private static final String URL = "/api/v1/receivables/{receivableId}/settlements";
    private static final UUID RECEIVABLE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
    private static final String IDEMPOTENCY_KEY = "settlement-test";
    private static final LocalDateTime SETTLED_AT = LocalDateTime.of(2026, 2, 1, 12, 0);
    private static final LocalDateTime VALID_FROM = LocalDateTime.of(2026, 1, 1, 0, 0);

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private ExchangeRatePort exchangeRatePort;

    @BeforeEach
    void prerequisites() {
        when(clock.instant()).thenReturn(Instant.parse("2026-02-01T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        saveReceivable(RECEIVABLE_ID, "PENDING");

        var baseRate = new BaseRateEntity();
        baseRate.setId(UUID.randomUUID());
        baseRate.setCurrency("BRL");
        baseRate.setRate(new BigDecimal("0.01"));
        baseRate.setValidFrom(VALID_FROM);
        baseRatesDataBase.save(baseRate);

        var typeRate = new ReceivableTypeRateEntity();
        typeRate.setId(UUID.randomUUID());
        typeRate.setTypeKey("DUPLICATA_MERCANTIL");
        typeRate.setSpread(new BigDecimal("0.015"));
        typeRate.setStrategyKey("STANDARD");
        typeRate.setValidFrom(VALID_FROM);
        receivableTypeRatesDataBase.save(typeRate);
    }

    @Nested
    @DisplayName("When Settle")
    class WhenSettle {

        @Test
        void deveLiquidarNaMesmaMoedaEPersistirCalculoEStatus() throws Exception {
            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "BRL", IDEMPOTENCY_KEY))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Idempotency-Replayed", "false"))
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.receivableId").value(RECEIVABLE_ID.toString()))
                    .andExpect(jsonPath("$.assignorId").value("CEDENTE-TESTE"))
                    .andExpect(jsonPath("$.faceValue").value("100000.000000"))
                    .andExpect(jsonPath("$.termMonths").value("3"))
                    .andExpect(jsonPath("$.baseRate").value("0.010000000000"))
                    .andExpect(jsonPath("$.baseRateReferenceDate").value("2026-01-01T00:00:00"))
                    .andExpect(jsonPath("$.spread").value("0.015000000000"))
                    .andExpect(jsonPath("$.spreadReferenceDate").value("2026-01-01T00:00:00"))
                    .andExpect(jsonPath("$.pricingStrategy").value("STANDARD"))
                    .andExpect(jsonPath("$.calculationMethod").value("PV=FV/(1+BASE_RATE+SPREAD)^TERM"))
                    .andExpect(jsonPath("$.calculationVersion").value(1))
                    .andExpect(jsonPath("$.presentValue").value("92859.94"))
                    .andExpect(jsonPath("$.presentValueCurrency").value("BRL"))
                    .andExpect(jsonPath("$.exchangeRate").value(nullValue()))
                    .andExpect(jsonPath("$.exchangeRateReferenceDate").value(nullValue()))
                    .andExpect(jsonPath("$.finalAmount").value("92859.94"))
                    .andExpect(jsonPath("$.finalCurrency").value("BRL"))
                    .andExpect(jsonPath("$.settledAt").value("2026-02-01T12:00:00"));

            var settlements = settlementsDataBase.findAll();
            assertThat(settlements).hasSize(1);
            var persisted = settlements.getFirst();
            assertThat(persisted.get("receivable_id")).isEqualTo(RECEIVABLE_ID);
            assertThat(persisted.get("idempotency_key")).isEqualTo(IDEMPOTENCY_KEY);
            assertThat(persisted.get("final_currency")).isEqualTo("BRL");
            assertThat((BigDecimal) persisted.get("present_value")).isEqualByComparingTo("92859.94");
            assertThat((BigDecimal) persisted.get("final_amount")).isEqualByComparingTo("92859.94");
            assertReceivableStatus(RECEIVABLE_ID, "SETTLED", 1);
            assertThat(idempotencyKeysDataBase.findAll()).hasSize(1);
            verifyNoInteractions(exchangeRatePort);
        }

        @Test
        void deveLiquidarComConversaoCambial() throws Exception {
            when(exchangeRatePort.find(Currency.USD, Currency.BRL, SETTLED_AT))
                    .thenReturn(new ExchangeRateQuote(new BigDecimal("5.4321"), SETTLED_AT));

            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "USD", IDEMPOTENCY_KEY))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Idempotency-Replayed", "false"))
                    .andExpect(jsonPath("$.presentValue").value("92859.94"))
                    .andExpect(jsonPath("$.presentValueCurrency").value("BRL"))
                    .andExpect(jsonPath("$.exchangeRate").value("5.4321"))
                    .andExpect(jsonPath("$.exchangeRateReferenceDate").value("2026-02-01T12:00:00"))
                    .andExpect(jsonPath("$.finalAmount").value("17094.67"))
                    .andExpect(jsonPath("$.finalCurrency").value("USD"));

            var settlements = settlementsDataBase.findAll();
            assertThat(settlements).hasSize(1);
            var persisted = settlements.getFirst();
            assertThat((BigDecimal) persisted.get("exchange_rate")).isEqualByComparingTo("5.4321");
            assertThat((BigDecimal) persisted.get("final_amount")).isEqualByComparingTo("17094.67");
            assertThat(persisted.get("final_currency")).isEqualTo("USD");
            assertReceivableStatus(RECEIVABLE_ID, "SETTLED", 1);
            verify(exchangeRatePort).find(Currency.USD, Currency.BRL, SETTLED_AT);
        }
    }

    @Nested
    @DisplayName("When Replay")
    class WhenReplay {

        @Test
        void deveReaproveitarLiquidacaoSemRecalcularOuDuplicarRegistros() throws Exception {
            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "BRL", IDEMPOTENCY_KEY))
                    .andExpect(status().isCreated());
            var settlementId = settlementsDataBase.findAll().getFirst().get("id").toString();
            baseRatesDataBase.deleteAll();
            receivableTypeRatesDataBase.deleteAll();
            when(clock.instant()).thenReturn(Instant.parse("2026-02-02T12:00:00Z"));

            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "BRL", IDEMPOTENCY_KEY))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Idempotency-Replayed", "true"))
                    .andExpect(jsonPath("$.id").value(settlementId))
                    .andExpect(jsonPath("$.receivableId").value(RECEIVABLE_ID.toString()))
                    .andExpect(jsonPath("$.finalAmount").value("92859.940000"))
                    .andExpect(jsonPath("$.finalCurrency").value("BRL"))
                    .andExpect(jsonPath("$.settledAt").value("2026-02-01T12:00:00"));

            assertThat(settlementsDataBase.findAll()).hasSize(1);
            assertThat(idempotencyKeysDataBase.findAll()).hasSize(1);
            assertReceivableStatus(RECEIVABLE_ID, "SETTLED", 1);
            verifyNoInteractions(exchangeRatePort);
        }
    }

    @Nested
    @DisplayName("When Conflict")
    class WhenConflict {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void deveRejeitarChaveReutilizadaComOutraRequisicao(boolean differentReceivable) throws Exception {
            var otherId = UUID.fromString("30000000-0000-0000-0000-000000000002");
            saveReceivable(otherId, "PENDING");
            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "BRL", IDEMPOTENCY_KEY))
                    .andExpect(status().isCreated());

            mockMvc.perform(settlementRequest(differentReceivable ? otherId : RECEIVABLE_ID,
                            differentReceivable ? "BRL" : "USD", IDEMPOTENCY_KEY))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.code").value("RECE003"));

            assertThat(settlementsDataBase.findAll()).hasSize(1);
            assertThat(idempotencyKeysDataBase.findAll()).hasSize(1);
            assertReceivableStatus(RECEIVABLE_ID, "SETTLED", 1);
            assertReceivableStatus(otherId, "PENDING", 0);
            verifyNoInteractions(exchangeRatePort);
        }

        @Test
        void deveRejeitarNovaChaveParaRecebivelComLiquidacao() throws Exception {
            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "BRL", IDEMPOTENCY_KEY))
                    .andExpect(status().isCreated());

            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "BRL", "outra-chave"))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.code").value("RECE004"));

            assertThat(settlementsDataBase.findAll()).hasSize(1);
            assertThat(idempotencyKeysDataBase.findAll()).hasSize(1);
            assertReceivableStatus(RECEIVABLE_ID, "SETTLED", 1);
        }

        @Test
        void deveRejeitarRecebivelComStatusLiquidado() throws Exception {
            receivableTypeRatesDataBase.execute("UPDATE receivables SET status = 'SETTLED' WHERE id = '" + RECEIVABLE_ID + "'");

            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "BRL", IDEMPOTENCY_KEY))
                    .andExpect(status().is(422))
                    .andExpect(jsonPath("$.code").value("RECE006"));

            assertThat(settlementsDataBase.findAll()).isEmpty();
            assertThat(idempotencyKeysDataBase.findAll()).isEmpty();
            assertReceivableStatus(RECEIVABLE_ID, "SETTLED", 0);
        }
    }

    @Nested
    @DisplayName("When Invalid Request")
    class WhenInvalidRequest {

        @Test
        void deveRejeitarHeaderDeIdempotenciaAusente() throws Exception {
            mockMvc.perform(post(URL, RECEIVABLE_ID).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"currency\":\"BRL\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXRA400"));
            assertNoSettlement();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 3, 121})
        void deveRejeitarChaveVaziaEmBrancoOuLonga(int length) throws Exception {
            var key = length <= 3 ? " ".repeat(length) : "a".repeat(length);
            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "BRL", key))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXRA400"));
            assertNoSettlement();
        }

        @ParameterizedTest
        @ValueSource(strings = {"{}", "{\"currency\":null}"})
        void deveRejeitarMoedaObrigatoriaNaoInformada(String body) throws Exception {
            mockMvc.perform(post(URL, RECEIVABLE_ID).header("Idempotency-Key", IDEMPOTENCY_KEY)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violacoes[0].campo").value("currency"))
                    .andExpect(jsonPath("$.violacoes[0].constraint").value("NotNull"));
            assertNoSettlement();
        }

        @Test
        void deveRejeitarMoedaInvalida() throws Exception {
            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "INVALIDA", IDEMPOTENCY_KEY))
                    .andExpect(status().isBadRequest());
            assertNoSettlement();
        }

        @Test
        void deveRejeitarIdentificadorInvalido() throws Exception {
            mockMvc.perform(post(URL, "id-invalido").header("Idempotency-Key", IDEMPOTENCY_KEY)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"currency\":\"BRL\"}"))
                    .andExpect(status().isBadRequest());
            assertNoSettlement();
        }
    }

    @Nested
    @DisplayName("When Settlement Fails")
    class WhenSettlementFails {

        @Test
        void deveRetornarNotFoundParaRecebivelInexistente() throws Exception {
            mockMvc.perform(settlementRequest(UUID.randomUUID(), "BRL", IDEMPOTENCY_KEY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RECE005"));
            assertNoSettlement();
        }

        @ParameterizedTest
        @CsvSource({"true, RECE001", "false, RECE002"})
        void deveDesfazerOperacaoSemConfiguracaoDePrecificacao(boolean missingBaseRate, String code) throws Exception {
            if (missingBaseRate) {
                baseRatesDataBase.deleteAll();
            } else {
                receivableTypeRatesDataBase.deleteAll();
            }

            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "BRL", IDEMPOTENCY_KEY))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(code));
            assertNoSettlement();
        }

        @Test
        void deveDesfazerOperacaoQuandoCotacaoIndisponivelEPermitirNovaTentativa() throws Exception {
            when(exchangeRatePort.find(Currency.USD, Currency.BRL, SETTLED_AT))
                    .thenThrow(new ServiceUnavailableException("CAMBIO503", "Câmbio indisponível", "Falha ao consultar cotação"))
                    .thenReturn(new ExchangeRateQuote(new BigDecimal("5.4321"), SETTLED_AT));

            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "USD", IDEMPOTENCY_KEY))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.code").value("CAMBIO503"));
            assertNoSettlement();

            mockMvc.perform(settlementRequest(RECEIVABLE_ID, "USD", IDEMPOTENCY_KEY))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Idempotency-Replayed", "false"))
                    .andExpect(jsonPath("$.finalAmount").value("17094.67"));
            assertThat(settlementsDataBase.findAll()).hasSize(1);
            assertThat(idempotencyKeysDataBase.findAll()).hasSize(1);
            assertReceivableStatus(RECEIVABLE_ID, "SETTLED", 1);
        }
    }

    private MockHttpServletRequestBuilder settlementRequest(UUID receivableId, String currency, String key) {
        return post(URL, receivableId).header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currency\":\"%s\"}".formatted(currency));
    }

    private void assertNoSettlement() {
        assertThat(settlementsDataBase.findAll()).isEmpty();
        assertThat(idempotencyKeysDataBase.findAll()).isEmpty();
        assertReceivableStatus(RECEIVABLE_ID, "PENDING", 0);
    }

    private void assertReceivableStatus(UUID id, String status, long version) {
        var receivables = receivablesDataBase.findAll();

        assertThat(receivables.stream()
                .filter(entry -> id.equals(entry.get("id")))
                .findFirst().orElse(null))
                .containsEntry("status", status)
                .containsEntry("version", version);
    }

    private void saveReceivable(UUID id, String status) {
        var receivable = new ReceivableEntity();
        receivable.setId(id);
        receivable.setAssignorId("CEDENTE-TESTE");
        receivable.setFaceValue(new BigDecimal("100000.00"));
        receivable.setCurrency("BRL");
        receivable.setAcquisitionDate(LocalDate.of(2026, 1, 1));
        receivable.setMaturityDate(LocalDate.of(2026, 4, 1));
        receivable.setType("DUPLICATA_MERCANTIL");
        receivable.setStatus(status);
        receivable.setCreatedAt(VALID_FROM);
        receivable.setUpdatedAt(VALID_FROM);
        receivablesDataBase.save(receivable);
    }
}
