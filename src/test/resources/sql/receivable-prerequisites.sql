-- Recebível necessário aos cenários de consulta e atualização.
INSERT INTO receivables (
    id, assignor_id, face_value, currency, acquisition_date, maturity_date,
    type, status, version, created_at, updated_at
) VALUES (
    '30000000-0000-0000-0000-000000000001', 'CEDENTE-TESTE', 1000.00, 'BRL',
    CURRENT_DATE, CURRENT_DATE + 90, 'DUPLICATA_MERCANTIL', 'PENDING', 0,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
