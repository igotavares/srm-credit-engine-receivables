package br.com.ibeans.receivables.adapter.in.web.pricing;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.BaseRateRequest;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.BaseRateResponse;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.TypeRateRequest;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.TypeRateResponse;
import br.com.ibeans.receivables.adapter.in.web.pricing.mapper.TypeRateInMapper;
import br.com.ibeans.receivables.application.port.in.pricing.PricingConfigurationUseCase;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.ReceivableType;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/pricing/configurations")
@RequiredArgsConstructor
class PricingConfigurationController implements SaggerPricingConfiguration {

    final PricingConfigurationUseCase useCase;
    final TypeRateInMapper typeRateInMapper;

    @GetMapping("/base-rates")
    public BaseRateResponse findBaseRate(
            @RequestParam("currency") Currency currency,
            @RequestParam("at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at
    ) {
        return BaseRateResponse.from(currency, useCase.findBaseRate(currency, at));
    }

    @PostMapping("/base-rates")
    @ResponseStatus(HttpStatus.CREATED)
    public BaseRateResponse addBaseRate(@Valid @RequestBody BaseRateRequest request) {
        return BaseRateResponse.from(
                request.currency(),
                useCase.addBaseRate(request.currency(),
                        request.rate(),
                        request.validFrom())
        );
    }

    @PostMapping("/receivable-types")
    @ResponseStatus(HttpStatus.CREATED)
    public Optional<TypeRateResponse> addTypeRate(@Valid @RequestBody TypeRateRequest request) {
        return Optional.ofNullable(useCase.addTypeRate(
                request.type(),
                request.spread(),
                request.strategyKey(),
                request.validFrom()
        )).map(typeRateInMapper::from);
    }

    @GetMapping("/receivable-types")
    public TypeRateResponse findReceivableTypeConfiguration(
            @RequestParam("type") ReceivableType type,
            @RequestParam("at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime at
    ) {
        return typeRateInMapper.from(useCase.findReceivableTypeConfiguration(type, at));
    }

}
