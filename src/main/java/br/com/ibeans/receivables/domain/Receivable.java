package br.com.ibeans.receivables.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Receivable(
        UUID id,
        String assignorId,
        BigDecimal faceValue,
        Currency currency,
        LocalDate acquisitionDate,
        LocalDate maturityDate,
        ReceivableType type,
        ReceivableStatus status,
        long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Receivable {
        if (id == null) throw new IllegalArgumentException("Id do recebível é obrigatório");
        if (assignorId == null || assignorId.isBlank()) throw new IllegalArgumentException("Cedente é obrigatório");
        if (faceValue == null || faceValue.signum() <= 0) throw new IllegalArgumentException("Valor de face deve ser maior que zero");
        if (currency == null) throw new IllegalArgumentException("Moeda do título é obrigatória");
        if (acquisitionDate == null || maturityDate == null) throw new IllegalArgumentException("Datas são obrigatórias");
        if (!maturityDate.isAfter(acquisitionDate)) throw new IllegalArgumentException("Vencimento deve ser posterior à aquisição");
        if (type == null) throw new IllegalArgumentException("Tipo do recebível é obrigatório");
        if (status == null) throw new IllegalArgumentException("Status do recebível é obrigatório");
    }

    public Receivable update(
            BigDecimal newFaceValue,
            LocalDate newMaturityDate,
            ReceivableType newType,
            LocalDateTime when
    ) {
        if (status == ReceivableStatus.SETTLED) {
            throw new IllegalStateException("Recebível liquidado é imutável");
        }
        return new Receivable(
                id,
                assignorId,
                newFaceValue,
                currency,
                acquisitionDate,
                newMaturityDate,
                newType,
                status,
                version,
                createdAt,
                when
        );
    }

    public Receivable settle(LocalDateTime when) {
        if (status == ReceivableStatus.SETTLED) {
            throw new IllegalStateException("Recebível já liquidado");
        }
        return new Receivable(
                id,
                assignorId,
                faceValue,
                currency,
                acquisitionDate,
                maturityDate,
                type,
                ReceivableStatus.SETTLED,
                version,
                createdAt,
                when
        );
    }
}
