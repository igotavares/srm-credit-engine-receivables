package br.com.ibeans.receivables.adapter.out.persistence.pricing.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "receivable_type_rates")
public class ReceivableTypeRateEntity {
    @Id UUID id;
    @Column(name = "type_key", nullable = false) String typeKey;
    @Column(nullable = false, precision = 18, scale = 12) BigDecimal spread;
    @Column(name = "strategy_key", nullable = false) String strategyKey;
    @Column(name = "valid_from", nullable = false) LocalDateTime validFrom;
}
