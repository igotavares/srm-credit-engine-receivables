package br.com.ibeans.receivables.adapter.out.persistence.settlement.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "settlements",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_settlement_receivable", columnNames = "receivable_id"),
                @UniqueConstraint(name = "uk_settlement_idempotency", columnNames = "idempotency_key")
        }
)
public class SettlementEntity {
    @Id UUID id;
    @Column(name = "receivable_id", nullable = false) UUID receivableId;
    @Column(name = "assignor_id", nullable = false) String assignorId;
    @Column(name = "face_value", nullable = false, precision = 18, scale = 6) BigDecimal faceValue;
    @Column(name = "term_months", nullable = false, precision = 18, scale = 12) BigDecimal termMonths;
    @Column(name = "base_rate", nullable = false, precision = 18, scale = 12) BigDecimal baseRate;
    @Column(name = "base_rate_reference_date", nullable = false) LocalDateTime baseRateReferenceDate;
    @Column(nullable = false, precision = 18, scale = 12) BigDecimal spread;
    @Column(name = "spread_reference_date", nullable = false) LocalDateTime spreadReferenceDate;
    @Column(name = "pricing_strategy", nullable = false) String pricingStrategy;
    @Column(name = "calculation_method", nullable = false) String calculationMethod;
    @Column(name = "calculation_version", nullable = false) Long calculationVersion;
    @Column(name = "present_value", nullable = false, precision = 18, scale = 6) BigDecimal presentValue;
    @Column(name = "present_value_currency", nullable = false, length = 3) String presentValueCurrency;
    @Column(name = "exchange_rate", precision = 18, scale = 8) BigDecimal exchangeRate;
    @Column(name = "exchange_rate_reference_date") LocalDateTime exchangeRateReferenceDate;
    @Column(name = "final_amount", nullable = false, precision = 18, scale = 6) BigDecimal finalAmount;
    @Column(name = "final_currency", nullable = false, length = 3) String finalCurrency;
    @Column(name = "settled_at", nullable = false) LocalDateTime settledAt;
    @Column(name = "idempotency_key", nullable = false, length = 120) String idempotencyKey;
}
