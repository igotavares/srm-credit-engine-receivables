package br.com.ibeans.receivables.adapter.out.persistence.lock;

import br.com.ibeans.receivables.application.port.out.IdempotencyLockPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Component
public class PostgresIdempotencyLockAdapter implements IdempotencyLockPort {

    private final JdbcTemplate jdbcTemplate;

    PostgresIdempotencyLockAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void lock(String idempotencyKey) {
        jdbcTemplate.update(
                """
                INSERT INTO idempotency_keys (idempotency_key, created_at)
                VALUES (?, CURRENT_TIMESTAMP)
                ON CONFLICT (idempotency_key) DO NOTHING
                """,
                idempotencyKey
        );

        jdbcTemplate.queryForObject(
                """
                SELECT idempotency_key
                  FROM idempotency_keys
                 WHERE idempotency_key = ?
                 FOR UPDATE
                """,
                String.class,
                idempotencyKey
        );
    }
}
