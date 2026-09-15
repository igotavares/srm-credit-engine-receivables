package br.com.ibeans.receivables.adapter.in.web.settlement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.application.port.in.settlement.SettlementReportResult;
import br.com.ibeans.receivables.domain.Currency;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportItemResponse(
        @Schema(description = "Identificador da liquidação.", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID settlementId,
        @Schema(description = "Identificador do recebível.", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID receivableId,
        @Schema(description = "Identificador do cedente.", example = "CEDENTE-001")
        String assignorId,
        @Schema(description = "Valor final na moeda do pagamento.", example = "8363.87")
        String finalAmount,
        @Schema(description = "Moeda da operação.", example = "BRL")
        Currency currency,
        @Schema(description = "Data e hora da liquidação, sem fuso horário.", example = "2026-09-14T10:00:00")
        LocalDateTime settledAt
) {
    public static ReportItemResponse from(SettlementReportResult.Item item) {
        return new ReportItemResponse(
                item.settlementId(),
                item.receivableId(),
                item.assignorId(),
                item.finalAmount().toPlainString(),
                item.currency(),
                item.settledAt()
        );
    }
}
