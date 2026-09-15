package br.com.ibeans.receivables.adapter.in.web.settlement;

import br.com.ibeans.receivables.adapter.in.web.settlement.dto.ReportItemResponse;
import br.com.ibeans.receivables.adapter.in.web.settlement.dto.ReportResponse;
import br.com.ibeans.receivables.application.port.in.settlement.SettlementReportQuery;
import br.com.ibeans.receivables.application.port.in.settlement.SettlementReportUseCase;
import br.com.ibeans.receivables.domain.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reports/settlements")
@RequiredArgsConstructor
class SettlementReportController implements SwaggerSettlementReport {

    final SettlementReportUseCase useCase;

    @GetMapping
    public ReportResponse find(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) String assignorId,
            @RequestParam(required = false) Currency currency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = useCase.execute(new SettlementReportQuery(
                from, to, assignorId, currency, page, size
        ));
        return new ReportResponse(
                result.items().stream()
                        .map(ReportItemResponse::from)
                        .toList(),
                result.total(),
                result.page(),
                result.size()
        );
    }

}
