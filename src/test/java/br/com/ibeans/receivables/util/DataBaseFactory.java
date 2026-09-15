package br.com.ibeans.receivables.util;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.PlatformTransactionManager;

public enum DataBaseFactory {

    SETTLEMENTS("settlements"),
    IDEMPOTENCY_KEYS("idempotency_keys"),
    RECEIVABLES("receivables"),
    BASE_RATES("base_rates"),
    RECEIVABLE_TYPE_RATES("receivable_type_rates");

    private String tableName;

    DataBaseFactory(String tableName) {
        this.tableName = tableName;
    }

    public DataBase create(EntityManager entityManager,
    PlatformTransactionManager transactionManager) {
        return new DataBase(entityManager, transactionManager, this.tableName);
    }

}
