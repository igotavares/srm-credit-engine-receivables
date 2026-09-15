package br.com.ibeans.receivables.adapter.out.persistence.pricing.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "base_rates")
public class BaseRateEntity {
    @Id UUID id;
    @Column(nullable = false, length = 3) String currency;
    @Column(nullable = false, precision = 18, scale = 12) BigDecimal rate;
    @Column(name = "valid_from", nullable = false) LocalDateTime validFrom;
}
