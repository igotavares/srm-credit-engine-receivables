package br.com.ibeans.receivables;

import br.com.ibeans.receivables.application.port.in.receivable.CreateReceivableCommand;
import br.com.ibeans.receivables.application.port.in.receivable.ReceivableUseCases;
import br.com.ibeans.receivables.application.port.in.settlement.SettleReceivableUseCase;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.ReceivableType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = "app.outbox.enabled=false")
class SettlementConcurrencyIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ReceivableUseCases useCases;

    @Autowired
    SettleReceivableUseCase settleReceivable;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void mesmaIdempotencyKeyDeveReaproveitarResultado() {
        var receivable = newReceivable();

        var first = settleReceivable.settle(
                receivable.id(),
                Currency.BRL,
                "idem-same-" + UUID.randomUUID()
        );

        var second = settleReceivable.settle(
                receivable.id(),
                Currency.BRL,
                first.settlement().idempotencyKey()
        );

        assertThat(first.replayed()).isFalse();
        assertThat(second.replayed()).isTrue();
        assertThat(second.settlement().id()).isEqualTo(first.settlement().id());
    }

    @Test
    void duasLiquidacoesConcorrentesComChavesDiferentesDevemGerarUmSettlement() throws Exception {
        var receivable = newReceivable();
        var gate = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Callable<Boolean> first = () -> attempt(receivable.id(), "idem-a-" + UUID.randomUUID(), gate);
            Callable<Boolean> second = () -> attempt(receivable.id(), "idem-b-" + UUID.randomUUID(), gate);

            var f1 = executor.submit(first);
            var f2 = executor.submit(second);
            gate.countDown();

            var successes = (f1.get() ? 1 : 0) + (f2.get() ? 1 : 0);

            assertThat(successes).isEqualTo(1);

            var count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM settlements WHERE receivable_id = ?",
                    Long.class,
                    receivable.id()
            );
            assertThat(count).isEqualTo(1L);
        }
    }

    private boolean attempt(UUID receivableId, String key, CountDownLatch gate) throws InterruptedException {
        gate.await();
        try {
            settleReceivable.settle(receivableId, Currency.BRL, key);
            return true;
        } catch (RuntimeException expectedConflict) {
            return false;
        }
    }

    private br.com.ibeans.receivables.domain.Receivable newReceivable() {
        var today = LocalDate.now();
        return useCases.create(new CreateReceivableCommand(
                "CEDENTE-TESTE-" + UUID.randomUUID(),
                new BigDecimal("100000.00"),
                Currency.BRL,
                today,
                today.plusDays(90),
                ReceivableType.DUPLICATA_MERCANTIL
        ));
    }
}
