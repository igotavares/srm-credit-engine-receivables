package br.com.ibeans.receivables.application.service;

import br.com.ibeans.receivables.application.port.in.settlement.SettlementReportUseCase;
import br.com.ibeans.receivables.application.port.in.settlement.SettlementReportQuery;
import br.com.ibeans.receivables.application.port.in.settlement.SettlementReportResult;
import br.com.ibeans.receivables.application.port.out.SettlementReportPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementReportService implements SettlementReportUseCase {

    private final SettlementReportPort reportPort;

    @Override
    @Transactional(readOnly = true)
    public SettlementReportResult execute(SettlementReportQuery query) {
        if (query.page() < 0) throw new IllegalArgumentException("page deve ser >= 0");
        if (query.size() < 1 || query.size() > 200) {
            throw new IllegalArgumentException("size deve estar entre 1 e 200");
        }

        var page = reportPort.find(
                query.from(),
                query.to(),
                query.assignorId(),
                query.currency(),
                query.page(),
                query.size()
        );

        return new SettlementReportResult(
                page.items().stream()
                        .map(item -> new SettlementReportResult.Item(
                                item.settlementId(),
                                item.receivableId(),
                                item.assignorId(),
                                item.finalAmount(),
                                item.currency(),
                                item.settledAt()
                        ))
                        .toList(),
                page.total(),
                page.page(),
                page.size()
        );
    }
}
