package br.com.ibeans.receivables.adapter.in.web.receivable.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.Receivable;
import br.com.ibeans.receivables.domain.ReceivableStatus;
import br.com.ibeans.receivables.domain.ReceivableType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReceivableResponse(
        @Schema(description = "Identificador do recurso.", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Identificador do cedente.", example = "CEDENTE-001")
        String assignorId,
        @Schema(description = "Valor nominal do recebível.", example = "10000.00")
        String faceValue,
        @Schema(description = "Moeda da operação.", example = "BRL")
        Currency currency,
        @Schema(description = "Data de aquisição.", example = "2026-09-14")
        LocalDate acquisitionDate,
        @Schema(description = "Data de vencimento.", example = "2027-09-14")
        LocalDate maturityDate,
        @Schema(description = "Tipo do recebível.", example = "DUPLICATA_MERCANTIL")
        ReceivableType type,
        @Schema(description = "Situação atual do recebível.", example = "PENDING")
        ReceivableStatus status,
        @Schema(description = "Versão do recebível para controle de concorrência.", example = "0")
        long version,
        @Schema(description = "Data e hora de criação, sem fuso horário.", example = "2026-09-14T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Data e hora da última atualização, sem fuso horário.", example = "2026-09-14T10:00:00")
        LocalDateTime updatedAt
) {
    public static ReceivableResponse from(Receivable domain) {
        return new ReceivableResponse(
                domain.id(),
                domain.assignorId(),
                domain.faceValue().toPlainString(),
                domain.currency(),
                domain.acquisitionDate(),
                domain.maturityDate(),
                domain.type(),
                domain.status(),
                domain.version(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
