CREATE TABLE receivables (
    id UUID PRIMARY KEY,
    assignor_id VARCHAR(100) NOT NULL,
    face_value NUMERIC(18,6) NOT NULL CHECK (face_value > 0),
    currency VARCHAR(3) NOT NULL,
    acquisition_date DATE NOT NULL,
    maturity_date DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT ck_receivable_dates CHECK (maturity_date > acquisition_date)
);

CREATE INDEX idx_receivable_assignor
    ON receivables (assignor_id);

CREATE TABLE base_rates (
    id UUID PRIMARY KEY,
    currency VARCHAR(3) NOT NULL,
    rate NUMERIC(18,12) NOT NULL CHECK (rate >= 0),
    valid_from TIMESTAMP NOT NULL,
    CONSTRAINT uk_base_rate_validity UNIQUE (currency, valid_from)
);

CREATE INDEX idx_base_rate_effective
    ON base_rates (currency, valid_from DESC);

CREATE TABLE receivable_type_rates (
    id UUID PRIMARY KEY,
    type_key VARCHAR(50) NOT NULL,
    spread NUMERIC(18,12) NOT NULL CHECK (spread >= 0),
    strategy_key VARCHAR(100) NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    CONSTRAINT uk_type_rate_validity UNIQUE (type_key, valid_from)
);

CREATE INDEX idx_type_rate_effective
    ON receivable_type_rates (type_key, valid_from DESC);

CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(120) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE settlements (
    id UUID PRIMARY KEY,
    receivable_id UUID NOT NULL,
    assignor_id VARCHAR(100) NOT NULL,
    face_value NUMERIC(18,6) NOT NULL,
    term_months NUMERIC(18,12) NOT NULL,
    base_rate NUMERIC(18,12) NOT NULL,
    base_rate_reference_date TIMESTAMP NOT NULL,
    spread NUMERIC(18,12) NOT NULL,
    spread_reference_date TIMESTAMP NOT NULL,
    pricing_strategy VARCHAR(100) NOT NULL,
    calculation_method VARCHAR(255) NOT NULL,
    calculation_version BIGINT NOT NULL,
    present_value NUMERIC(18,6) NOT NULL,
    present_value_currency VARCHAR(3) NOT NULL,
    exchange_rate NUMERIC(18,8),
    exchange_rate_reference_date TIMESTAMP,
    final_amount NUMERIC(18,6) NOT NULL,
    final_currency VARCHAR(3) NOT NULL,
    settled_at TIMESTAMP NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    CONSTRAINT fk_settlement_receivable
        FOREIGN KEY (receivable_id) REFERENCES receivables(id),
    CONSTRAINT uk_settlement_receivable UNIQUE (receivable_id),
    CONSTRAINT uk_settlement_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX idx_settlement_report
    ON settlements (settled_at DESC, assignor_id, final_currency);

INSERT INTO base_rates (id, currency, rate, valid_from)
VALUES
('10000000-0000-0000-0000-000000000001', 'BRL', 0.010000000000, TIMESTAMP '2020-01-01 00:00:00'),
('10000000-0000-0000-0000-000000000002', 'USD', 0.004000000000, TIMESTAMP '2020-01-01 00:00:00');

INSERT INTO receivable_type_rates (id, type_key, spread, strategy_key, valid_from)
VALUES
('20000000-0000-0000-0000-000000000001', 'DUPLICATA_MERCANTIL', 0.015000000000, 'STANDARD', TIMESTAMP '2020-01-01 00:00:00'),
('20000000-0000-0000-0000-000000000002', 'CHEQUE_PRE_DATADO', 0.025000000000, 'STANDARD', TIMESTAMP '2020-01-01 00:00:00');
