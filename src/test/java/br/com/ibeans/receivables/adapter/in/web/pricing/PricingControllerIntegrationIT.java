package br.com.ibeans.receivables.adapter.in.web.pricing;

import br.com.ibeans.receivables.adapter.in.web.AbstractControllerIT;
import br.com.ibeans.receivables.adapter.out.persistence.pricing.entity.BaseRateEntity;
import br.com.ibeans.receivables.adapter.out.persistence.pricing.entity.ReceivableTypeRateEntity;
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
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PricingControllerIntegrationIT extends AbstractControllerIT {

    private static final String URL = "/api/v1/pricing/simulations";
    private static final LocalDateTime REFERENCE_DATE = LocalDateTime.of(2026, 2, 1, 12, 0);
    private static final LocalDateTime VALID_FROM = LocalDateTime.of(2026, 1, 1, 0, 0);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private ExchangeRatePort exchangeRatePort;

    @BeforeEach
    void configureClock() {
        when(clock.instant()).thenReturn(Instant.parse("2026-02-01T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    @Nested
    @DisplayName("When Simulate")
    class WhenSimulate {

        @BeforeEach
        void prerequisites() {
            saveBaseRate("0.01", VALID_FROM);
            saveTypeRate("DUPLICATA_MERCANTIL", "0.015", VALID_FROM);
            saveTypeRate("CHEQUE_PRE_DATADO", "0.025", VALID_FROM);
        }

        @ParameterizedTest
        @CsvSource({
                "DUPLICATA_MERCANTIL, 100000.00, 2026-04-01, 3, 0.015000000000, 92859.94, 7140.06",
                "CHEQUE_PRE_DATADO, 25000.00, 2026-03-02, 2, 0.025000000000, 23337.77, 1662.23"
        })
        void deveSimularNaMesmaMoedaSemPersistirRecebivelOuLiquidacao(
                String type, String faceValue, String maturityDate, String term,
                String spread, String presentValue, String discount) throws Exception {
            var request = simulationRequest();
            request.put("type", type);
            request.put("faceValue", new BigDecimal(faceValue));
            request.put("maturityDate", maturityDate);

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.termMonths").value(term))
                    .andExpect(jsonPath("$.baseRate").value("0.010000000000"))
                    .andExpect(jsonPath("$.baseRateReferenceDate").value("2026-01-01T00:00:00"))
                    .andExpect(jsonPath("$.spread").value(spread))
                    .andExpect(jsonPath("$.spreadReferenceDate").value("2026-01-01T00:00:00"))
                    .andExpect(jsonPath("$.pricingStrategy").value("STANDARD"))
                    .andExpect(jsonPath("$.calculationMethod").value("PV=FV/(1+BASE_RATE+SPREAD)^TERM"))
                    .andExpect(jsonPath("$.calculationVersion").value(1))
                    .andExpect(jsonPath("$.presentValue").value(presentValue))
                    .andExpect(jsonPath("$.presentValueCurrency").value("BRL"))
                    .andExpect(jsonPath("$.discount").value(discount))
                    .andExpect(jsonPath("$.exchangeRate").value(nullValue()))
                    .andExpect(jsonPath("$.exchangeRateReferenceDate").value(nullValue()))
                    .andExpect(jsonPath("$.finalAmount").value(presentValue))
                    .andExpect(jsonPath("$.finalCurrency").value("BRL"));

            assertThat(receivablesDataBase.findAll()).isEmpty();
            assertThat(settlementsDataBase.findAll()).isEmpty();
            assertThat(idempotencyKeysDataBase.findAll()).isEmpty();
            verifyNoInteractions(exchangeRatePort);
        }

        @Test
        void deveConverterParaMoedaDePagamento() throws Exception {
            when(exchangeRatePort.find(Currency.USD, Currency.BRL, REFERENCE_DATE))
                    .thenReturn(new ExchangeRateQuote(new BigDecimal("5.4321"), REFERENCE_DATE));
            var request = simulationRequest();
            request.put("paymentCurrency", "USD");

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.presentValue").value("92859.94"))
                    .andExpect(jsonPath("$.presentValueCurrency").value("BRL"))
                    .andExpect(jsonPath("$.exchangeRate").value("5.4321"))
                    .andExpect(jsonPath("$.exchangeRateReferenceDate").value("2026-02-01T12:00:00"))
                    .andExpect(jsonPath("$.finalAmount").value("17094.67"))
                    .andExpect(jsonPath("$.finalCurrency").value("USD"));

            verify(exchangeRatePort).find(Currency.USD, Currency.BRL, REFERENCE_DATE);
            assertThat(receivablesDataBase.findAll()).isEmpty();
            assertThat(settlementsDataBase.findAll()).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void deveUsarDataAtualQuandoReferenciaNaoInformada(boolean omitReferenceDate) throws Exception {
            saveBaseRate("0.02", REFERENCE_DATE);
            saveTypeRate("DUPLICATA_MERCANTIL", "0.03", REFERENCE_DATE);
            saveBaseRate("0.50", REFERENCE_DATE.plusSeconds(1));
            saveTypeRate("DUPLICATA_MERCANTIL", "0.50", REFERENCE_DATE.plusSeconds(1));
            var request = simulationRequest();
            if (omitReferenceDate) {
                request.remove("referenceDate");
            } else {
                request.put("referenceDate", null);
            }

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.baseRate").value("0.020000000000"))
                    .andExpect(jsonPath("$.spread").value("0.030000000000"))
                    .andExpect(jsonPath("$.baseRateReferenceDate").value("2026-02-01T12:00:00"))
                    .andExpect(jsonPath("$.spreadReferenceDate").value("2026-02-01T12:00:00"));
        }

        @Test
        void deveSelecionarTaxasVigentesNaReferenciaInformada() throws Exception {
            saveBaseRate("0.02", REFERENCE_DATE);
            saveTypeRate("DUPLICATA_MERCANTIL", "0.03", REFERENCE_DATE);
            var request = simulationRequest();
            request.put("referenceDate", "2026-01-15T12:00:00");

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.baseRate").value("0.010000000000"))
                    .andExpect(jsonPath("$.spread").value("0.015000000000"))
                    .andExpect(jsonPath("$.baseRateReferenceDate").value("2026-01-01T00:00:00"))
                    .andExpect(jsonPath("$.spreadReferenceDate").value("2026-01-01T00:00:00"))
                    .andExpect(jsonPath("$.finalAmount").value("92859.94"));
        }
    }

    @Nested
    @DisplayName("When Invalid Request")
    class WhenInvalidRequest {

        @ParameterizedTest
        @ValueSource(strings = {"faceValue", "receivableCurrency", "acquisitionDate", "maturityDate", "type", "paymentCurrency"})
        void deveRejeitarCampoObrigatorioAusente(String field) throws Exception {
            var request = simulationRequest();
            request.remove(field);

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violacoes[0].campo").value(field))
                    .andExpect(jsonPath("$.violacoes[0].constraint").value("NotNull"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1"})
        void deveRejeitarValorDeFaceNaoPositivo(String faceValue) throws Exception {
            var request = simulationRequest();
            request.put("faceValue", new BigDecimal(faceValue));

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violacoes[0].campo").value("faceValue"))
                    .andExpect(jsonPath("$.violacoes[0].constraint").value("DecimalMin"));
        }

        @ParameterizedTest
        @CsvSource({
                "2026-01-01, 2026-01-31, Data de vencimento não pode estar no passado",
                "2026-04-01, 2026-04-01, Vencimento deve ser posterior à aquisição",
                "2026-04-02, 2026-04-01, Vencimento deve ser posterior à aquisição"
        })
        void deveRejeitarDatasInvalidas(String acquisitionDate, String maturityDate, String detail) throws Exception {
            var request = simulationRequest();
            request.put("acquisitionDate", acquisitionDate);
            request.put("maturityDate", maturityDate);

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("EXRA400"))
                    .andExpect(jsonPath("$.detail").value(detail));
        }
    }

    @Nested
    @DisplayName("When Configuration Not Found")
    class WhenConfigurationNotFound {

        @Test
        void deveRetornarNotFoundSemTaxaBaseVigente() throws Exception {
            saveBaseRate("0.01", REFERENCE_DATE.plusSeconds(1));
            saveTypeRate("DUPLICATA_MERCANTIL", "0.015", VALID_FROM);

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(simulationRequest())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RECE001"));
        }

        @Test
        void deveRetornarNotFoundSemSpreadVigente() throws Exception {
            saveBaseRate("0.01", VALID_FROM);
            saveTypeRate("DUPLICATA_MERCANTIL", "0.015", REFERENCE_DATE.plusSeconds(1));

            mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(simulationRequest())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RECE002"));
        }
    }

    private Map<String, Object> simulationRequest() {
        var request = new LinkedHashMap<String, Object>();
        request.put("faceValue", new BigDecimal("100000.00"));
        request.put("receivableCurrency", "BRL");
        request.put("acquisitionDate", "2026-01-01");
        request.put("maturityDate", "2026-04-01");
        request.put("type", "DUPLICATA_MERCANTIL");
        request.put("paymentCurrency", "BRL");
        request.put("referenceDate", REFERENCE_DATE.toString());
        return request;
    }

    private void saveBaseRate(String rate, LocalDateTime validFrom) {
        var entity = new BaseRateEntity();
        entity.setId(UUID.randomUUID());
        entity.setCurrency("BRL");
        entity.setRate(new BigDecimal(rate));
        entity.setValidFrom(validFrom);
        baseRatesDataBase.save(entity);
    }

    private void saveTypeRate(String type, String spread, LocalDateTime validFrom) {
        var entity = new ReceivableTypeRateEntity();
        entity.setId(UUID.randomUUID());
        entity.setTypeKey(type);
        entity.setSpread(new BigDecimal(spread));
        entity.setStrategyKey("STANDARD");
        entity.setValidFrom(validFrom);
        receivableTypeRatesDataBase.save(entity);
    }
}
