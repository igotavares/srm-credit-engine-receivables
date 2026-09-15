package br.com.ibeans.receivables.adapter.in.web.pricing;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.PricingResponse;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.SimulationRequest;
import br.com.ibeans.receivables.adapter.in.web.pricing.mapper.PricingInMapper;
import br.com.ibeans.receivables.adapter.in.web.pricing.mapper.SimulationInMapper;
import br.com.ibeans.receivables.application.port.in.pricing.SimulatePricingUseCase;
import br.com.ibeans.receivables.application.port.in.pricing.SimulationCommand;
import br.com.ibeans.receivables.domain.pricing.PricingCalculation;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;
import br.com.ibeans.receivables.domain.ExchangeRateQuote;
import br.com.ibeans.receivables.domain.ReceivableType;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingControllerTest {

    @Mock
    SimulatePricingUseCase mockUseCase;

    @Mock
    SimulationInMapper mockSimulationInMapper;

    @Mock
    PricingInMapper mockPricingInMapper;

    @InjectMocks
    PricingController controller;

    @Nested
    @DisplayName("When Simulate")
    class WhenSimulate {

        @Test
        @DisplayName("Should Return Mapped Pricing Response")
        void shouldReturnMappedPricingResponse() {
            var request = request();
            var command = command();
            var calculation = calculation();
            var response = response();
            when(mockSimulationInMapper.from(request)).thenReturn(command);
            when(mockUseCase.simulate(command)).thenReturn(calculation);
            when(mockPricingInMapper.from(calculation)).thenReturn(response);

            var result = controller.simulate(request);

            assertThat(result).hasValueSatisfying(actual -> assertThat(actual)
                    .as("Mapped pricing response must contain all expected fields")
                    .usingRecursiveComparison()
                    .isEqualTo(response()));
            verify(mockSimulationInMapper).from(argThat(actual -> matchesAllFields(actual, request())));
            verify(mockUseCase).simulate(argThat(actual -> matchesAllFields(actual, command())));
            verify(mockPricingInMapper).from(argThat(actual -> matchesAllFields(actual, calculation())));
        }

        @Test
        @DisplayName("Given Request Is Null Then Should Return Empty")
        void shouldReturnEmptyWhenRequestIsNull() {
            assertThat(controller.simulate(null)).isEmpty();

            verifyNoInteractions(mockSimulationInMapper, mockUseCase, mockPricingInMapper);
        }

        @Test
        @DisplayName("Request Mapper Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenRequestMapperReturnsNull() {
            var request = request();
            when(mockSimulationInMapper.from(request)).thenReturn(null);

            assertThat(controller.simulate(request)).isEmpty();

            verify(mockSimulationInMapper).from(argThat(actual -> matchesAllFields(actual, request())));
            verifyNoInteractions(mockUseCase, mockPricingInMapper);
        }

        @Test
        @DisplayName("Use Case Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenUseCaseReturnsNull() {
            var request = request();
            var command = command();
            when(mockSimulationInMapper.from(request)).thenReturn(command);
            when(mockUseCase.simulate(command)).thenReturn(null);

            assertThat(controller.simulate(request)).isEmpty();

            verify(mockSimulationInMapper).from(argThat(actual -> matchesAllFields(actual, request())));
            verify(mockUseCase).simulate(argThat(actual -> matchesAllFields(actual, command())));
            verifyNoInteractions(mockPricingInMapper);
        }

        @Test
        @DisplayName("Response Mapper Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenResponseMapperReturnsNull() {
            var request = request();
            var command = command();
            var calculation = calculation();
            when(mockSimulationInMapper.from(request)).thenReturn(command);
            when(mockUseCase.simulate(command)).thenReturn(calculation);
            when(mockPricingInMapper.from(calculation)).thenReturn(null);

            assertThat(controller.simulate(request)).isEmpty();

            verify(mockSimulationInMapper).from(argThat(actual -> matchesAllFields(actual, request())));
            verify(mockUseCase).simulate(argThat(actual -> matchesAllFields(actual, command())));
            verify(mockPricingInMapper).from(argThat(actual -> matchesAllFields(actual, calculation())));
        }

        @Test
        @DisplayName("Use Case Throws Exception Then Should Propagate Exception")
        void shouldPropagateUseCaseException() {
            var request = request();
            var command = command();
            var exception = new IllegalStateException("Pricing configuration not found");
            when(mockSimulationInMapper.from(request)).thenReturn(command);
            when(mockUseCase.simulate(command)).thenThrow(exception);

            assertThatThrownBy(() -> controller.simulate(request)).isSameAs(exception);

            verify(mockSimulationInMapper).from(argThat(actual -> matchesAllFields(actual, request())));
            verify(mockUseCase).simulate(argThat(actual -> matchesAllFields(actual, command())));

            verifyNoInteractions(mockPricingInMapper);
        }
    }


    boolean matchesAllFields(Object actual, Object expected) {
        assertThat(actual)
                .as("All fields of %s must match", expected.getClass().getSimpleName())
                .usingRecursiveComparison()
                .isEqualTo(expected);
        return true;
    }

    SimulationRequest request() {
        return new SimulationRequest(new BigDecimal("10000.00"), Currency.USD,
                LocalDate.of(2026, 9, 14), LocalDate.of(2027, 9, 14),
                ReceivableType.DUPLICATA_MERCANTIL, Currency.BRL,
                LocalDateTime.of(2026, 9, 14, 10, 0));
    }

    SimulationCommand command() {
        return new SimulationCommand(new BigDecimal("10000.00"), Currency.USD,
                LocalDate.of(2026, 9, 14), LocalDate.of(2027, 9, 14),
                ReceivableType.DUPLICATA_MERCANTIL, Currency.BRL,
                LocalDateTime.of(2026, 9, 14, 10, 0));
    }

    PricingCalculation calculation() {
        return new PricingCalculation(new BigDecimal("12"),
                new EffectiveRate(new BigDecimal("0.01"), LocalDateTime.of(2026, 9, 14, 8, 0)),
                new ReceivableTypeConfiguration(ReceivableType.DUPLICATA_MERCANTIL,
                        new BigDecimal("0.005"), "STANDARD", LocalDateTime.of(2026, 9, 14, 9, 0)),
                "STANDARD", "PV=FV/(1+BASE_RATE+SPREAD)^TERM", 1L,
                new BigDecimal("8363.87"), Currency.USD, new BigDecimal("1636.13"),
                new ExchangeRateQuote(new BigDecimal("5.00"), LocalDateTime.of(2026, 9, 14, 10, 0)),
                new BigDecimal("41819.35"), Currency.BRL);
    }

    PricingResponse response() {
        return new PricingResponse("12", "0.01", LocalDateTime.of(2026, 9, 14, 8, 0),
                "0.005", LocalDateTime.of(2026, 9, 14, 9, 0),
                "STANDARD", "PV=FV/(1+BASE_RATE+SPREAD)^TERM", 1L,
                "8363.87", Currency.USD, "1636.13", "5.00",
                LocalDateTime.of(2026, 9, 14, 10, 0), "41819.35", Currency.BRL);
    }
}
