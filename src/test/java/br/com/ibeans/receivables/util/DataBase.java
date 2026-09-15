package br.com.ibeans.receivables.util;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DataBase {

    final EntityManager entityManager;
    final PlatformTransactionManager transactionManager;
    final String tableName;

    private TransactionTemplate isolatedTransaction() {
        var transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transaction;
    }

    public void execute(String sql) {
        isolatedTransaction().executeWithoutResult(status -> {
            var query = entityManager.createNativeQuery(sql);
            query.executeUpdate();
        });
    }

    public void save(Object entity) {
        isolatedTransaction().executeWithoutResult(status -> {
            entityManager.persist(entity);
        });
    }

    public void deleteAll() {
        isolatedTransaction().executeWithoutResult(status -> {
            entityManager.createNativeQuery("delete from " + tableName).executeUpdate();
        });
        entityManager.clear();
    }

    public List<Map<String, Object>> findAll() {
        return isolatedTransaction().execute(status -> entityManager
                .createNativeQuery("select r.* from " + tableName + " r", Map.class)
                .getResultList());
    }


}
