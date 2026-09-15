package br.com.ibeans.receivables.adapter.in.web.pricing;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.PricingResponse;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.SimulationRequest;
import br.com.ibeans.receivables.adapter.in.web.pricing.mapper.PricingInMapper;
import br.com.ibeans.receivables.adapter.in.web.pricing.mapper.SimulationInMapper;
import br.com.ibeans.receivables.application.port.in.pricing.SimulatePricingUseCase;
import br.com.ibeans.receivables.application.port.in.pricing.SimulationCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
public class PricingController implements SwaggerPricing {

    final SimulatePricingUseCase useCase;
    final SimulationInMapper simulationInMapper;
    final PricingInMapper pricingInMapper;

    @PostMapping("/simulations")
    public Optional<PricingResponse> simulate(@Valid @RequestBody SimulationRequest request) {
        return Optional.ofNullable(request)
                .map(simulationInMapper::from)
                .map(useCase::simulate)
                .map(pricingInMapper::from);
    }

}
