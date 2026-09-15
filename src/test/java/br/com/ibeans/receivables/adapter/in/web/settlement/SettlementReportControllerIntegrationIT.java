package br.com.ibeans.receivables.adapter.in.web.settlement;

import br.com.ibeans.receivables.adapter.in.web.AbstractControllerIT;
import br.com.ibeans.receivables.adapter.out.persistence.receivable.entity.ReceivableEntity;
import br.com.ibeans.receivables.adapter.out.persistence.settlement.entity.SettlementEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SettlementReportControllerIntegrationIT extends AbstractControllerIT {

    private static final String URL = "/api/v1/reports/settlements";
    private static final String FIRST_ID = "40000000-0000-0000-0000-000000000001";
    private static final String SECOND_ID = "40000000-0000-0000-0000-000000000002";
    private static final String THIRD_ID = "40000000-0000-0000-0000-000000000003";
    private static final String FOURTH_ID = "40000000-0000-0000-0000-000000000004";

    @Nested
    @DisplayName("When Find")
    class WhenFind {

        @BeforeEach
        void prerequisites() {
            saveSettlement(FIRST_ID, "CEDENTE-A", "BRL", "92859.94", "2026-02-01T10:00:00");
            saveSettlement(SECOND_ID, "CEDENTE-B", "USD", "17094.67", "2026-02-02T10:00:00");
            saveSettlement(THIRD_ID, "CEDENTE-A", "USD", "17094.67", "2026-02-03T10:00:00");
            saveSettlement(FOURTH_ID, "CEDENTE-A", "BRL", "92859.94", "2026-02-03T10:00:00");
        }

        @Test
        void deveListarComPaginacaoPadraoOrdenandoPorDataEIdDecrescentes() throws Exception {
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(4))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.items[*].settlementId", contains(FOURTH_ID, THIRD_ID, SECOND_ID, FIRST_ID)))
                    .andExpect(jsonPath("$.items[0].receivableId").value(receivableId(FOURTH_ID).toString()))
                    .andExpect(jsonPath("$.items[0].assignorId").value("CEDENTE-A"))
                    .andExpect(jsonPath("$.items[0].finalAmount").value("92859.940000"))
                    .andExpect(jsonPath("$.items[0].currency").value("BRL"))
                    .andExpect(jsonPath("$.items[0].settledAt").value("2026-02-03T10:00:00"))
                    .andExpect(jsonPath("$.items[1].finalAmount").value("17094.670000"))
                    .andExpect(jsonPath("$.items[1].currency").value("USD"));
        }

        @Test
        void deveFiltrarPorDataInicialInclusiva() throws Exception {
            mockMvc.perform(get(URL).param("from", "2026-02-02T10:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(3))
                    .andExpect(jsonPath("$.items[*].settlementId", contains(FOURTH_ID, THIRD_ID, SECOND_ID)));
        }

        @Test
        void deveFiltrarPorDataFinalInclusiva() throws Exception {
            mockMvc.perform(get(URL).param("to", "2026-02-02T10:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(2))
                    .andExpect(jsonPath("$.items[*].settlementId", contains(SECOND_ID, FIRST_ID)));
        }

        @Test
        void deveFiltrarIntervaloComInicioEFimIguais() throws Exception {
            mockMvc.perform(get(URL).param("from", "2026-02-02T10:00:00")
                            .param("to", "2026-02-02T10:00:00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.items[*].settlementId", contains(SECOND_ID)));
        }

        @Test
        void deveFiltrarPorCedente() throws Exception {
            mockMvc.perform(get(URL).param("assignorId", "CEDENTE-A"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(3))
                    .andExpect(jsonPath("$.items[*].settlementId", contains(FOURTH_ID, THIRD_ID, FIRST_ID)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        void deveIgnorarFiltroDeCedenteEmBranco(String assignorId) throws Exception {
            mockMvc.perform(get(URL).param("assignorId", assignorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(4))
                    .andExpect(jsonPath("$.items.length()").value(4));
        }

        @Test
        void deveFiltrarPelaMoedaFinalDaLiquidacao() throws Exception {
            mockMvc.perform(get(URL).param("currency", "USD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(2))
                    .andExpect(jsonPath("$.items[*].settlementId", contains(THIRD_ID, SECOND_ID)))
                    .andExpect(jsonPath("$.items[*].currency", contains("USD", "USD")));
        }

        @Test
        void deveCombinarTodosOsFiltros() throws Exception {
            mockMvc.perform(get(URL).param("from", "2026-02-02T10:00:00")
                            .param("to", "2026-02-03T10:00:00")
                            .param("assignorId", "CEDENTE-A").param("currency", "USD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.items[*].settlementId", contains(THIRD_ID)));
        }

        @ParameterizedTest
        @CsvSource({
                "assignorId, INEXISTENTE",
                "assignorId, CEDENTE",
                "from, 2026-02-04T00:00:00",
                "to, 2026-02-01T09:59:59"
        })
        void deveRetornarListaVaziaQuandoFiltroNaoEncontraLiquidacoes(String parameter, String value) throws Exception {
            mockMvc.perform(get(URL).param(parameter, value))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(0))
                    .andExpect(jsonPath("$.items").isEmpty());
        }

        @ParameterizedTest
        @CsvSource({"0, " + FOURTH_ID + ", " + THIRD_ID, "1, " + SECOND_ID + ", " + FIRST_ID})
        void devePaginarPreservandoTotalEOrdenacao(int page, String firstId, String secondId) throws Exception {
            mockMvc.perform(get(URL).param("page", Integer.toString(page)).param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(4))
                    .andExpect(jsonPath("$.page").value(page))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.items[*].settlementId", contains(firstId, secondId)));
        }

        @Test
        void devePaginarResultadosFiltradosComTotalDoFiltro() throws Exception {
            mockMvc.perform(get(URL).param("assignorId", "CEDENTE-A").param("page", "1").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(3))
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.items[*].settlementId", contains(FIRST_ID)));
        }

        @Test
        void deveRetornarPaginaVaziaPreservandoTotal() throws Exception {
            mockMvc.perform(get(URL).param("page", "2").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(4))
                    .andExpect(jsonPath("$.page").value(2))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.items").isEmpty());
        }

        @ParameterizedTest
        @CsvSource({"1, 1", "200, 4"})
        void deveAceitarLimitesDoTamanhoDaPagina(int size, int count) throws Exception {
            mockMvc.perform(get(URL).param("size", Integer.toString(size)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(size))
                    .andExpect(jsonPath("$.total").value(4))
                    .andExpect(jsonPath("$.items.length()").value(count));
        }
    }

    @Nested
    @DisplayName("When Empty")
    class WhenEmpty {

        @Test
        void deveRetornarRelatorioVazioSemLiquidacoes() throws Exception {
            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isEmpty())
                    .andExpect(jsonPath("$.total").value(0))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20));
        }
    }

    @Nested
    @DisplayName("When Invalid Request")
    class WhenInvalidRequest {

        @ParameterizedTest
        @CsvSource({
                "page, -1, page deve ser >= 0",
                "size, 0, size deve estar entre 1 e 200",
                "size, -1, size deve estar entre 1 e 200",
                "size, 201, size deve estar entre 1 e 200"
        })
        void deveRejeitarPaginacaoForaDosLimites(String parameter, String value, String detail) throws Exception {
            mockMvc.perform(get(URL).param(parameter, value))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXRA400"))
                    .andExpect(jsonPath("$.detail").value(detail));
        }

        @ParameterizedTest
        @CsvSource({"from, data-invalida", "to, data-invalida", "currency, INVALIDA", "page, abc", "size, abc"})
        void deveRejeitarParametrosComFormatoInvalido(String parameter, String value) throws Exception {
            mockMvc.perform(get(URL).param(parameter, value))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXRA400"));
        }
    }

    private UUID receivableId(String settlementId) {
        return UUID.fromString(settlementId.replaceFirst("40000000", "30000000"));
    }

    private void saveSettlement(String id, String assignorId, String currency, String finalAmount, String settledAt) {
        var date = LocalDateTime.parse(settledAt);
        var referenceDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        var receivable = new ReceivableEntity();
        receivable.setId(receivableId(id));
        receivable.setAssignorId(assignorId);
        receivable.setFaceValue(new BigDecimal("100000.00"));
        receivable.setCurrency("BRL");
        receivable.setAcquisitionDate(LocalDate.of(2026, 1, 1));
        receivable.setMaturityDate(LocalDate.of(2026, 4, 1));
        receivable.setType("DUPLICATA_MERCANTIL");
        receivable.setStatus("SETTLED");
        receivable.setCreatedAt(referenceDate);
        receivable.setUpdatedAt(date);
        receivablesDataBase.save(receivable);

        var settlement = new SettlementEntity();
        settlement.setId(UUID.fromString(id));
        settlement.setReceivableId(receivable.getId());
        settlement.setAssignorId(assignorId);
        settlement.setFaceValue(receivable.getFaceValue());
        settlement.setTermMonths(new BigDecimal("3"));
        settlement.setBaseRate(new BigDecimal("0.01"));
        settlement.setBaseRateReferenceDate(referenceDate);
        settlement.setSpread(new BigDecimal("0.015"));
        settlement.setSpreadReferenceDate(referenceDate);
        settlement.setPricingStrategy("STANDARD");
        settlement.setCalculationMethod("PV=FV/(1+BASE_RATE+SPREAD)^TERM");
        settlement.setCalculationVersion(1L);
        settlement.setPresentValue(new BigDecimal("92859.94"));
        settlement.setPresentValueCurrency("BRL");
        if ("USD".equals(currency)) {
            settlement.setExchangeRate(new BigDecimal("5.4321"));
            settlement.setExchangeRateReferenceDate(date);
        }
        settlement.setFinalAmount(new BigDecimal(finalAmount));
        settlement.setFinalCurrency(currency);
        settlement.setSettledAt(date);
        settlement.setIdempotencyKey("report-" + id);
        settlementsDataBase.save(settlement);
    }
}
