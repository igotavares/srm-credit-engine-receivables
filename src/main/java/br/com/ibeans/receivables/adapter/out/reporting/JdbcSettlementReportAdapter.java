package br.com.ibeans.receivables.adapter.out.reporting;

import br.com.ibeans.receivables.application.port.out.SettlementReportPort;
import br.com.ibeans.receivables.domain.Currency;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
class JdbcSettlementReportAdapter implements SettlementReportPort {

    private final NamedParameterJdbcTemplate jdbc;

    JdbcSettlementReportAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public ReportPage find(
            LocalDateTime from,
            LocalDateTime to,
            String assignorId,
            Currency currency,
            int page,
            int size
    ) {
        var where = new ArrayList<String>();
        var params = new MapSqlParameterSource();

        if (from != null) {
            where.add("settled_at >= :from");
            params.addValue("from", from);
        }
        if (to != null) {
            where.add("settled_at <= :to");
            params.addValue("to", to);
        }
        if (assignorId != null && !assignorId.isBlank()) {
            where.add("assignor_id = :assignorId");
            params.addValue("assignorId", assignorId);
        }
        if (currency != null) {
            where.add("final_currency = :currency");
            params.addValue("currency", currency.name());
        }

        var whereClause = where.isEmpty() ? "" : " WHERE " + String.join(" AND ", where);

        var countSql = "SELECT COUNT(*) FROM settlements" + whereClause;
        var total = jdbc.queryForObject(countSql, params, Long.class);

        params.addValue("limit", size);
        params.addValue("offset", (long) page * size);

        var dataSql = """
                SELECT id, receivable_id, assignor_id, final_amount, final_currency, settled_at
                  FROM settlements
                """ + whereClause + """
                 ORDER BY settled_at DESC, id DESC
                 LIMIT :limit OFFSET :offset
                """;

        var items = jdbc.query(dataSql, params, this::map);
        return new ReportPage(items, total == null ? 0 : total, page, size);
    }

    private ReportItem map(ResultSet rs, int rowNum) throws SQLException {
        return new ReportItem(
                rs.getObject("id", java.util.UUID.class),
                rs.getObject("receivable_id", java.util.UUID.class),
                rs.getString("assignor_id"),
                rs.getBigDecimal("final_amount"),
                Currency.valueOf(rs.getString("final_currency")),
                rs.getTimestamp("settled_at").toLocalDateTime()
        );
    }
}
