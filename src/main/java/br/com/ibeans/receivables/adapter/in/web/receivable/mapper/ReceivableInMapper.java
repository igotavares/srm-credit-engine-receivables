package br.com.ibeans.receivables.adapter.in.web.receivable.mapper;

import br.com.ibeans.receivables.adapter.in.web.receivable.dto.CreateReceivableRequest;
import br.com.ibeans.receivables.adapter.in.web.receivable.dto.ReceivableResponse;
import br.com.ibeans.receivables.adapter.in.web.receivable.dto.UpdateReceivableRequest;
import br.com.ibeans.receivables.application.port.in.receivable.CreateReceivableCommand;
import br.com.ibeans.receivables.application.port.in.receivable.UpdateReceivableCommand;
import br.com.ibeans.receivables.domain.Receivable;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReceivableInMapper {

    CreateReceivableCommand from(CreateReceivableRequest request);

    ReceivableResponse from(Receivable domain);

    UpdateReceivableCommand from(UpdateReceivableRequest request);

}
