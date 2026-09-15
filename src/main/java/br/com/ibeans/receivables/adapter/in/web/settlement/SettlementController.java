package br.com.ibeans.receivables.adapter.in.web.settlement;

import br.com.ibeans.receivables.adapter.in.web.settlement.dto.SettlementRequest;
import br.com.ibeans.receivables.adapter.in.web.settlement.dto.SettlementResponse;
import br.com.ibeans.receivables.application.port.in.settlement.SettleReceivableUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/receivables/{receivableId}/settlements")
@RequiredArgsConstructor
class SettlementController implements SwaggerSettlement {

    final SettleReceivableUseCase useCase;

    @PostMapping
    public ResponseEntity<SettlementResponse> settle(
            @PathVariable UUID receivableId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody SettlementRequest request
    ) {
        var result = useCase.settle(receivableId, request.currency(), idempotencyKey);
        var status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status)
                .header("Idempotency-Replayed", Boolean.toString(result.replayed()))
                .body(SettlementResponse.from(result.settlement()));
    }

}
