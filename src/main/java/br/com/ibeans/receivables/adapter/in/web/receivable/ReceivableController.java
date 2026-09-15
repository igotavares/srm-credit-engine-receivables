package br.com.ibeans.receivables.adapter.in.web.receivable;

import br.com.ibeans.receivables.adapter.in.web.receivable.dto.CreateReceivableRequest;
import br.com.ibeans.receivables.adapter.in.web.receivable.dto.ReceivableResponse;
import br.com.ibeans.receivables.adapter.in.web.receivable.dto.UpdateReceivableRequest;
import br.com.ibeans.receivables.adapter.in.web.receivable.mapper.ReceivableInMapper;
import br.com.ibeans.receivables.application.port.in.receivable.CreateReceivableCommand;
import br.com.ibeans.receivables.application.port.in.receivable.UpdateReceivableCommand;
import br.com.ibeans.receivables.application.port.in.receivable.ReceivableUseCases;
import br.com.ibeans.receivables.domain.Receivable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/receivables")
@RequiredArgsConstructor
public class ReceivableController implements SwaggerPricing {

    final ReceivableUseCases useCase;
    final ReceivableInMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Optional<ReceivableResponse> create(@Valid @RequestBody CreateReceivableRequest request) {
        return Optional.ofNullable(request)
                .map(mapper::from)
                .map(useCase::create)
                .map(mapper::from);
    }

    @PutMapping("/{id}")
    public Optional<ReceivableResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReceivableRequest request
    ) {
        return Optional.ofNullable(request)
                .map(mapper::from)
                .map(update(id))
                .map(mapper::from);
    }

    private Function<UpdateReceivableCommand, Receivable> update(UUID id) {
        return command -> useCase.update(id, command);
    }

    @GetMapping("/{id}")
    public Optional<ReceivableResponse> find(@PathVariable UUID id) {
        return Optional.ofNullable(id)
                .map(useCase::find)
                .map(mapper::from);
    }

}
