package br.com.ibeans.receivables.application.port.out;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.ExchangeRateQuote;

import java.time.LocalDateTime;

public interface ExchangeRatePort {

    ExchangeRateQuote find(Currency baseCurrency, Currency quoteCurrency, LocalDateTime at);

}
