package br.com.ibeans.receivables;

import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

public abstract class AbstractTest {

    @Getter
    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        clear();
        now = LocalDateTime.now();
        context();
    }

    protected void context() {}

    protected void clear() {}

    @AfterEach
    public void tearDown() {
        clear();
    }

}
