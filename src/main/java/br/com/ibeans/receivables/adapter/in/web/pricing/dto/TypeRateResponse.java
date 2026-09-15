package br.com.ibeans.receivables.adapter.in.web.pricing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import br.com.ibeans.receivables.domain.ReceivableType;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;

import java.time.LocalDateTime;

public record TypeRateResponse(
        @Schema(description = "Tipo do recebível.", example = "DUPLICATA_MERCANTIL")
        ReceivableType type,
        @Schema(description = "Spread em formato decimal; 0.005 representa 0,5%.", example = "0.005")
        String spread,
        @Schema(description = "Chave da estratégia de precificação.", example = "STANDARD")
        String strategyKey,
        @Schema(description = "Início de vigência da configuração, sem fuso horário.", example = "2026-09-01T00:00:00")
        LocalDateTime validFrom
) { }
