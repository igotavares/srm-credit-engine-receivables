package br.com.ibeans.receivables.adapter.out.reporting;

import br.com.ibeans.receivables.application.port.out.SettlementReportPort;
import br.com.ibeans.receivables.domain.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcSettlementReportAdapterTest {
    private static final LocalDateTime FROM = LocalDateTime.of(2026, 9, 1, 0, 0);
    private static final LocalDateTime TO = LocalDateTime.of(2026, 9, 14, 23, 59);
    @Mock NamedParameterJdbcTemplate jdbc;
    private JdbcSettlementReportAdapter adapter;

    @BeforeEach void setUp() { adapter = new JdbcSettlementReportAdapter(jdbc); }

    @Test void shouldBuildQueryWithAllFiltersAndPagination() {
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class))).thenReturn(3L);
        when(jdbc.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class))).thenReturn(List.of());

        var result = adapter.find(FROM, TO, "CEDENTE-1", Currency.USD, 2, 10);

        assertThat(result).isEqualTo(new SettlementReportPort.ReportPage(List.of(), 3, 2, 10));
        var sql = ArgumentCaptor.forClass(String.class);
        var params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbc).queryForObject(sql.capture(), params.capture(), eq(Long.class));
        assertThat(sql.getValue()).contains("settled_at >= :from", "settled_at <= :to",
                "assignor_id = :assignorId", "final_currency = :currency");
        assertThat(params.getValue().getValue("from")).isEqualTo(FROM);
        assertThat(params.getValue().getValue("to")).isEqualTo(TO);
        assertThat(params.getValue().getValue("assignorId")).isEqualTo("CEDENTE-1");
        assertThat(params.getValue().getValue("currency")).isEqualTo("USD");
        verify(jdbc).query(argThat(s -> s.contains("LIMIT :limit OFFSET :offset")), any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    @Test void shouldIgnoreBlankAssignorAndUseZeroWhenCountIsNull() {
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class))).thenReturn(null);
        when(jdbc.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class))).thenReturn(List.of());

        var result = adapter.find(null, null, "   ", null, 0, 20);

        assertThat(result.total()).isZero();
        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForObject(sql.capture(), any(MapSqlParameterSource.class), eq(Long.class));
        assertThat(sql.getValue()).isEqualTo("SELECT COUNT(*) FROM settlements");
    }

    @Test void shouldReturnItemsProvidedByJdbcQuery() {
        var item = new SettlementReportPort.ReportItem(UUID.randomUUID(), UUID.randomUUID(), "CEDENTE-1",
                new BigDecimal("970.50"), Currency.BRL, TO);
        when(jdbc.queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class))).thenReturn(1L);
        when(jdbc.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class))).thenReturn(List.of(item));

        var result = adapter.find(null, TO, null, Currency.BRL, 0, 10);

        assertThat(result.items()).containsExactly(item);
        assertThat(result.total()).isEqualTo(1);
    }
}
