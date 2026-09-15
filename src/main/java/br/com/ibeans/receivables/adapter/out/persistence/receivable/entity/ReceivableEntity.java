package br.com.ibeans.receivables.adapter.out.persistence.receivable.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "receivables")
public class ReceivableEntity {
    @Id
    UUID id;

    @Column(name = "assignor_id", nullable = false)
    String assignorId;

    @Column(name = "face_value", nullable = false, precision = 18, scale = 6)
    BigDecimal faceValue;

    @Column(nullable = false, length = 3)
    String currency;

    @Column(name = "acquisition_date", nullable = false)
    LocalDate acquisitionDate;

    @Column(name = "maturity_date", nullable = false)
    LocalDate maturityDate;

    @Column(nullable = false)
    String type;

    @Column(nullable = false)
    String status;

    @Version
    long version;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

}
