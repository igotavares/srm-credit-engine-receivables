package br.com.ibeans.receivables.adapter.out.exchangerate;

import br.com.ibeans.receivables.adapter.out.exchangerate.dto.ExchangeRateResponse;
import br.com.ibeans.receivables.domain.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExchangeRateHttpAdapterTest {

    private static final LocalDateTime AT = LocalDateTime.of(2026, 9, 14, 10, 0);

    private MockRestServiceServer server;
    private ExchangeRateHttpAdapter adapter;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder().baseUrl("http://currency-engine");
        server = MockRestServiceServer.bindTo(builder).build();
        var restClient = builder.build();
        var retryTemplate = RetryTemplate.builder().maxAttempts(3).fixedBackoff(1).build();
        adapter = new ExchangeRateHttpAdapter(restClient, retryTemplate);
    }

    @Test
    void shouldReturnExchangeRateQuote() {
        var validFrom = LocalDateTime.of(2026, 9, 14, 9, 0);
        server.expect(requestTo("http://currency-engine/api/v1/exchange-rates/USD/BRL?at=2026-09-14T10:00"))
                .andExpect(queryParam("at", AT.toString()))
                .andRespond(withSuccess("""
                        {"id":"40000000-0000-0000-0000-000000000001","baseCurrency":"USD","quoteCurrency":"BRL","rate":"5.4321","validFrom":"2026-09-14T09:00:00"}
                        """, MediaType.APPLICATION_JSON));

        var result = adapter.find(Currency.USD, Currency.BRL, AT);

        assertThat(result.rate()).isEqualByComparingTo("5.4321");
        assertThat(result.validFrom()).isEqualTo(validFrom);
        server.verify();
    }

    @Test
    void shouldRejectNullResponse() {
        server.expect(requestTo("http://currency-engine/api/v1/exchange-rates/USD/BRL?at=2026-09-14T10:00"))
                .andRespond(withSuccess());

        assertThatThrownBy(() -> adapter.find(Currency.USD, Currency.BRL, AT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Currency Engine retornou taxa inválida");
    }

    @Test
    void shouldRejectResponseWithoutRate() {
        server.expect(requestTo("http://currency-engine/api/v1/exchange-rates/USD/BRL?at=2026-09-14T10:00"))
                .andRespond(withSuccess("{\"rate\":null,\"validFrom\":\"2026-09-14T09:00:00\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.find(Currency.USD, Currency.BRL, AT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Currency Engine retornou taxa inválida");
    }

    @Test
    void shouldRejectNonPositiveRate() {
        server.expect(requestTo("http://currency-engine/api/v1/exchange-rates/USD/BRL?at=2026-09-14T10:00"))
                .andRespond(withSuccess("{\"rate\":\"0\",\"validFrom\":\"2026-09-14T09:00:00\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> adapter.find(Currency.USD, Currency.BRL, AT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Currency Engine retornou taxa inválida");
    }

    @Test
    void shouldRetryCommunicationFailure() {
        server.expect(requestTo("http://currency-engine/api/v1/exchange-rates/USD/BRL?at=2026-09-14T10:00"))
                .andRespond(withServerError());
        server.expect(requestTo("http://currency-engine/api/v1/exchange-rates/USD/BRL?at=2026-09-14T10:00"))
                .andRespond(withServerError());
        server.expect(requestTo("http://currency-engine/api/v1/exchange-rates/USD/BRL?at=2026-09-14T10:00"))
                .andRespond(withSuccess("{\"rate\":\"5.00\",\"validFrom\":\"2026-09-14T09:00:00\"}", MediaType.APPLICATION_JSON));

        var result = adapter.find(Currency.USD, Currency.BRL, AT);

        assertThat(result.rate()).isEqualByComparingTo("5.00");
        server.verify();
    }
}
