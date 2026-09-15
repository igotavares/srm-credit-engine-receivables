package br.com.ibeans.receivables;

import br.com.ibeans.receivables.util.DataBase;
import br.com.ibeans.receivables.util.DataBaseFactory;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@SpringBootTest(properties = {
        "spring.docker.compose.skip.in-tests=false"
})
public abstract class AbstractIT extends AbstractTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    PlatformTransactionManager transactionManager;

    protected DataBase settlementsDataBase;
    protected DataBase idempotencyKeysDataBase;
    protected DataBase receivablesDataBase;
    protected DataBase baseRatesDataBase;
    protected DataBase receivableTypeRatesDataBase;
    protected List<DataBase> dataBases;

    @PostConstruct
    public void init() {
        settlementsDataBase = DataBaseFactory.SETTLEMENTS.create(entityManager, transactionManager);
        idempotencyKeysDataBase = DataBaseFactory.IDEMPOTENCY_KEYS.create(entityManager, transactionManager);
        receivablesDataBase = DataBaseFactory.RECEIVABLES.create(entityManager, transactionManager);
        baseRatesDataBase = DataBaseFactory.BASE_RATES.create(entityManager, transactionManager);
        receivableTypeRatesDataBase = DataBaseFactory.RECEIVABLE_TYPE_RATES.create(entityManager, transactionManager);
        this.dataBases = List.of(settlementsDataBase,
        idempotencyKeysDataBase,
        receivablesDataBase,
        baseRatesDataBase,
        receivableTypeRatesDataBase);
    }

    protected void clear() {
        dataBases.stream().forEach(DataBase::deleteAll);
    }

}
