package br.com.ibeans.receivables.domain.pricing;

public interface PricingStrategy {
    String key();
    String calculationMethod();
    long calculationVersion();
    PricingResult calculate(PricingContext context);
}
