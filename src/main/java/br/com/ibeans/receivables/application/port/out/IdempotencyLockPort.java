package br.com.ibeans.receivables.application.port.out;

public interface IdempotencyLockPort {
    void lock(String idempotencyKey);
}
