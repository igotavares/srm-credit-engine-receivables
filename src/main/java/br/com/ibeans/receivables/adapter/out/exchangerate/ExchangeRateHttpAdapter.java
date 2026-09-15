package br.com.ibeans.receivables.adapter.out.exchangerate;

import br.com.ibeans.receivables.adapter.out.exchangerate.dto.ExchangeRateResponse;
import br.com.ibeans.receivables.application.port.out.ExchangeRatePort;
import br.com.ibeans.receivables.domain.ExchangeRateQuote;
import br.com.ibeans.receivables.domain.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
class ExchangeRateHttpAdapter implements ExchangeRatePort {

    final RestClient restClient;
    final RetryTemplate retryTemplate;

    @Override
    public ExchangeRateQuote find(
            Currency baseCurrency,
            Currency quoteCurrency,
            LocalDateTime at
    ) {
        var response = retryTemplate.execute(context ->
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/v1/exchange-rates/{base}/{quote}")
                                .queryParam("at", at)
                                .build(baseCurrency.name(), quoteCurrency.name()))
                        .retrieve()
                        .body(ExchangeRateResponse.class)
        );

        if (response == null || response.rate() == null) {
            throw new IllegalStateException("Currency Engine retornou taxa inválida");
        }

        var rate = new BigDecimal(response.rate());
        if (rate.signum() <= 0) {
            throw new IllegalStateException("Currency Engine retornou taxa inválida");
        }

        return new ExchangeRateQuote(rate, response.validFrom());
    }

}
