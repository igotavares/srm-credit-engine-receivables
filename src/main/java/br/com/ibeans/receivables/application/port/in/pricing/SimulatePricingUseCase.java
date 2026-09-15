package br.com.ibeans.receivables.application.port.in.pricing;

import br.com.ibeans.receivables.domain.pricing.PricingCalculation;

public interface SimulatePricingUseCase {

    PricingCalculation simulate(SimulationCommand command);

}
