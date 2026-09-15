package br.com.ibeans.receivables.application.port.in.settlement;

public interface SettlementReportUseCase {

    SettlementReportResult execute(SettlementReportQuery query);

}
