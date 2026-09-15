package br.com.ibeans.receivables.adapter.in.web.pricing;

import br.com.ibeans.receivables.adapter.in.web.AbstractControllerIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PricingConfigurationControllerIntegrationIT extends AbstractControllerIT {

    private static final String URL = "/api/v1/pricing/configurations";
    private static final String VALID_FROM = "2026-01-01T10:00:00";

    @Nested
    @DisplayName("When Create Base Rate")
    class WhenCreateBaseRate {

        @ParameterizedTest
        @ValueSource(strings = {"0.012345678901", "0"})
        void deveCadastrarTaxaBaseEPersistirNoBanco(String rate) throws Exception {
            mockMvc.perform(post(URL + "/base-rates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(baseRateRequest(rate)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.currency").value("BRL"))
                    .andExpect(jsonPath("$.rate").value(rate))
                    .andExpect(jsonPath("$.validFrom").value(VALID_FROM));

            var rates = baseRatesDataBase.findAll();
            assertThat(rates).hasSize(1);
            var persisted = rates.getFirst();
            assertThat(persisted.get("id")).isNotNull();
            assertThat(persisted.get("currency")).isEqualTo("BRL");
            assertThat((BigDecimal) persisted.get("rate")).isEqualByComparingTo(rate);
        }

        @ParameterizedTest
        @CsvSource({
                "null, 0.01, '2026-01-01T10:00:00', currency, NotNull",
                "BRL, null, '2026-01-01T10:00:00', rate, NotNull",
                "BRL, -0.01, '2026-01-01T10:00:00', rate, DecimalMin",
                "BRL, 0.01, null, validFrom, NotNull"
        })
        void deveRejeitarCamposInvalidos(String currency, String rate, String validFrom,
                                        String field, String constraint) throws Exception {
            mockMvc.perform(post(URL + "/base-rates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"currency": %s, "rate": %s, "validFrom": %s}
                                    """.formatted(jsonString(currency), rate, jsonString(validFrom))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violacoes[0].campo").value(field))
                    .andExpect(jsonPath("$.violacoes[0].constraint").value(constraint));

            assertThat(baseRatesDataBase.findAll()).isEmpty();
        }

        @Test
        void deveRejeitarTaxaBaseComMesmaMoedaEVigencia() throws Exception {
            mockMvc.perform(post(URL + "/base-rates").contentType(MediaType.APPLICATION_JSON)
                            .content(baseRateRequest("0.01")))
                    .andExpect(status().isCreated());

            mockMvc.perform(post(URL + "/base-rates").contentType(MediaType.APPLICATION_JSON)
                            .content(baseRateRequest("0.02")))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EXRA409"));

            var rates = baseRatesDataBase.findAll();
            assertThat(rates).hasSize(1);
            assertThat((BigDecimal) rates.getFirst().get("rate")).isEqualByComparingTo("0.01");
        }
    }

    @Nested
    @DisplayName("When Create Type Rate")
    class WhenCreateTypeRate {

        @ParameterizedTest
        @ValueSource(strings = {"0.023456789012", "0"})
        void deveCadastrarSpreadPorTipoEPersistirNoBanco(String spread) throws Exception {
            mockMvc.perform(post(URL + "/receivable-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(typeRateRequest(spread, "STANDARD")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value("DUPLICATA_MERCANTIL"))
                    .andExpect(jsonPath("$.spread").value(spread))
                    .andExpect(jsonPath("$.strategyKey").value("STANDARD"))
                    .andExpect(jsonPath("$.validFrom").value(VALID_FROM));

            var rates = receivableTypeRatesDataBase.findAll();
            assertThat(rates).hasSize(1);
            var persisted = rates.getFirst();
            assertThat(persisted.get("id")).isNotNull();
            assertThat(persisted.get("type_key")).isEqualTo("DUPLICATA_MERCANTIL");
            assertThat((BigDecimal) persisted.get("spread")).isEqualByComparingTo(spread);
            assertThat(persisted.get("strategy_key")).isEqualTo("STANDARD");
        }

        @ParameterizedTest
        @CsvSource({
                "null, 0.01, STANDARD, '2026-01-01T10:00:00', type, NotNull",
                "DUPLICATA_MERCANTIL, null, STANDARD, '2026-01-01T10:00:00', spread, NotNull",
                "DUPLICATA_MERCANTIL, -0.01, STANDARD, '2026-01-01T10:00:00', spread, DecimalMin",
                "DUPLICATA_MERCANTIL, 0.01, null, '2026-01-01T10:00:00', strategyKey, NotBlank",
                "DUPLICATA_MERCANTIL, 0.01, '', '2026-01-01T10:00:00', strategyKey, NotBlank",
                "DUPLICATA_MERCANTIL, 0.01, '   ', '2026-01-01T10:00:00', strategyKey, NotBlank",
                "DUPLICATA_MERCANTIL, 0.01, STANDARD, null, validFrom, NotNull"
        })
        void deveRejeitarCamposInvalidos(String type, String spread, String strategyKey, String validFrom,
                                        String field, String constraint) throws Exception {
            mockMvc.perform(post(URL + "/receivable-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"type": %s, "spread": %s, "strategyKey": %s, "validFrom": %s}
                                    """.formatted(jsonString(type), spread, jsonString(strategyKey), jsonString(validFrom))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violacoes[0].campo").value(field))
                    .andExpect(jsonPath("$.violacoes[0].constraint").value(constraint));

            assertThat(receivableTypeRatesDataBase.findAll()).isEmpty();
        }

        @Test
        void deveRejeitarEstrategiaInexistente() throws Exception {
            mockMvc.perform(post(URL + "/receivable-types").contentType(MediaType.APPLICATION_JSON)
                            .content(typeRateRequest("0.01", "INEXISTENTE")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXRA400"))
                    .andExpect(jsonPath("$.detail").value("Strategy de precificação não encontrada: INEXISTENTE"));

            assertThat(receivableTypeRatesDataBase.findAll()).isEmpty();
        }

        @Test
        void deveRejeitarSpreadComMesmoTipoEVigencia() throws Exception {
            mockMvc.perform(post(URL + "/receivable-types").contentType(MediaType.APPLICATION_JSON)
                            .content(typeRateRequest("0.01", "STANDARD")))
                    .andExpect(status().isCreated());

            mockMvc.perform(post(URL + "/receivable-types").contentType(MediaType.APPLICATION_JSON)
                            .content(typeRateRequest("0.02", "STANDARD")))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("EXRA409"));

            var rates = receivableTypeRatesDataBase.findAll();
            assertThat(rates).hasSize(1);
            assertThat((BigDecimal) rates.getFirst().get("spread")).isEqualByComparingTo("0.01");
        }
    }

    @ParameterizedTest
    @CsvSource({"base-rates, currency, BRL", "receivable-types, type, DUPLICATA_MERCANTIL"})
    void deveConsultarConfiguracaoMaisRecenteAteDataInformada(String path, String key, String value) throws Exception {
        String request = path.equals("base-rates")
                ? baseRateRequest("0.02") : typeRateRequest("0.02", "STANDARD");
        for (String date : new String[]{VALID_FROM, "2026-02-01T10:00:00", "2026-03-01T10:00:00"}) {
            mockMvc.perform(post(URL + "/" + path).contentType(MediaType.APPLICATION_JSON)
                            .content(request.replace(VALID_FROM, date)))
                    .andExpect(status().isCreated());
        }
        for (String at : new String[]{"2026-02-01T10:00:00", "2026-02-15T10:00:00"}) {
            mockMvc.perform(get(URL + "/" + path)
                            .param(key, value)
                            .param("at", at))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$." + key).value(value))
                    .andExpect(jsonPath("$.validFrom").value("2026-02-01T10:00:00"));
        }
        mockMvc.perform(get(URL + "/" + path)
                        .param(key, value)
                        .param("at", "2026-01-01T09:59:59"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @CsvSource({"base-rates, currency, BRL, RECE001", "receivable-types, type, DUPLICATA_MERCANTIL, RECE002"})
    void deveRetornar404SemConfiguracao(String path, String key, String value, String code) throws Exception {
        mockMvc.perform(get(URL + "/" + path).param(key, value).param("at", VALID_FROM))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(code));
    }

    @ParameterizedTest
    @CsvSource({"base-rates, currency, BRL", "receivable-types, type, DUPLICATA_MERCANTIL"})
    void deveRejeitarParametrosDeConsultaAusentesOuInvalidos(String path, String key, String value) throws Exception {
        mockMvc.perform(get(URL + "/" + path)
                        .param(key, value))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(URL + "/" + path).param("at", VALID_FROM))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(URL + "/" + path)
                        .param(key, value)
                        .param("at", "data-invalida"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(URL + "/" + path)
                        .param(key, "INVALIDO")
                        .param("at", VALID_FROM))
                .andExpect(status().isBadRequest());
    }

    private static String baseRateRequest(String rate) {
        return """
                {"currency": "BRL", "rate": %s, "validFrom": "%s"}
                """.formatted(rate, VALID_FROM);
    }

    private static String typeRateRequest(String spread, String strategyKey) {
        return """
                {"type": "DUPLICATA_MERCANTIL", "spread": %s, "strategyKey": "%s", "validFrom": "%s"}
                """.formatted(spread, strategyKey, VALID_FROM);
    }

    private static String jsonString(String value) {
        return "null".equals(value) ? "null" : "\"" + value + "\"";
    }
}
