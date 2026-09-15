-- Executado somente no PostgreSQL isolado do teste; preserva o histórico do Flyway.
TRUNCATE TABLE settlements, idempotency_keys, receivables,
    base_rates, receivable_type_rates RESTART IDENTITY;
