package br.com.ibeans.receivables.adapter.in.web.pricing.mapper;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.TypeRateResponse;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TypeRateInMapper {

    TypeRateResponse from(ReceivableTypeConfiguration config);

}
