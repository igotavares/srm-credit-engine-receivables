package br.com.ibeans.receivables.adapter.in.web.pricing.mapper;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.SimulationRequest;
import br.com.ibeans.receivables.application.port.in.pricing.SimulationCommand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SimulationInMapper {

    SimulationCommand from(SimulationRequest request);

}
