package br.com.ibeans.receivables.adapter.in.web.pricing.mapper;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.PricingResponse;
import br.com.ibeans.receivables.domain.pricing.PricingCalculation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.math.BigDecimal;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PricingInMapper {

    @Mapping(target = "baseRate", source = "baseRate.rate")
    @Mapping(target = "baseRateReferenceDate", source = "baseRate.validFrom")
    @Mapping(target = "spread", source = "typeConfiguration.spread")
    @Mapping(target = "spreadReferenceDate", source = "typeConfiguration.validFrom")
    @Mapping(target = "exchangeRate", source = "exchangeRate.rate")
    @Mapping(target = "exchangeRateReferenceDate", source = "exchangeRate.validFrom")
    PricingResponse from(PricingCalculation result);

    default String toPlainString(BigDecimal value) {
        return value != null ? value.toPlainString() : null;
    }

}
