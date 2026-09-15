package br.com.ibeans.receivables.adapter.in.web.pricing;

import br.com.ibeans.receivables.adapter.in.web.pricing.dto.BaseRateRequest;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.BaseRateResponse;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.TypeRateRequest;
import br.com.ibeans.receivables.adapter.in.web.pricing.dto.TypeRateResponse;
import br.com.ibeans.receivables.adapter.in.web.pricing.mapper.TypeRateInMapper;
import br.com.ibeans.receivables.application.port.in.pricing.PricingConfigurationUseCase;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.EffectiveRate;
import br.com.ibeans.receivables.domain.ReceivableType;
import br.com.ibeans.receivables.domain.ReceivableTypeConfiguration;
import java.math.BigDecimal;
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
class PricingConfigurationControllerTest {

    @Mock
    PricingConfigurationUseCase mockUseCase;

    @Mock
    TypeRateInMapper mockTypeRateInMapper;

    @InjectMocks
    PricingConfigurationController controller;

    @Nested
    @DisplayName("When Find Base Rate")
    class WhenFindBaseRate {

        @Test
        @DisplayName("Should Return Base Rate Response")
        void shouldReturnBaseRateResponse() {
            var currency = Currency.BRL;
            var at = LocalDateTime.of(2026, 9, 14, 10, 0);
            var rate = effectiveRate();
            when(mockUseCase.findBaseRate(currency, at)).thenReturn(rate);

            var result = controller.findBaseRate(currency, at);

            assertThat(result)
                    .as("Base rate response must contain currency and effective rate fields")
                    .usingRecursiveComparison()
                    .isEqualTo(baseRateResponse());
            verify(mockUseCase).findBaseRate(currency, at);
            verifyNoInteractions(mockTypeRateInMapper);
        }

        @Test
        @DisplayName("Use Case Throws Exception Then Should Propagate Exception")
        void shouldPropagateUseCaseException() {
            var currency = Currency.BRL;
            var at = LocalDateTime.of(2026, 9, 14, 10, 0);
            var exception = new IllegalStateException("Base rate not found");
            when(mockUseCase.findBaseRate(currency, at)).thenThrow(exception);

            assertThatThrownBy(() -> controller.findBaseRate(currency, at)).isSameAs(exception);

            verify(mockUseCase).findBaseRate(currency, at);
            verifyNoInteractions(mockTypeRateInMapper);
        }
    }

    @Nested
    @DisplayName("When Add Base Rate")
    class WhenAddBaseRate {

        @Test
        @DisplayName("Should Return Created Base Rate Response")
        void shouldReturnCreatedBaseRateResponse() {
            var request = baseRateRequest();
            var rate = effectiveRate();
            when(mockUseCase.addBaseRate(request.currency(), request.rate(), request.validFrom())).thenReturn(rate);

            var result = controller.addBaseRate(request);

            assertThat(result)
                    .as("Created base rate response must contain request currency and effective rate fields")
                    .usingRecursiveComparison()
                    .isEqualTo(baseRateResponse());
            verify(mockUseCase).addBaseRate(request.currency(), request.rate(), request.validFrom());
            verifyNoInteractions(mockTypeRateInMapper);
        }

        @Test
        @DisplayName("Use Case Throws Exception Then Should Propagate Exception")
        void shouldPropagateUseCaseException() {
            var request = baseRateRequest();
            var exception = new IllegalStateException("Duplicated base rate");
            when(mockUseCase.addBaseRate(request.currency(), request.rate(), request.validFrom()))
                    .thenThrow(exception);

            assertThatThrownBy(() -> controller.addBaseRate(request)).isSameAs(exception);

            verify(mockUseCase).addBaseRate(request.currency(), request.rate(), request.validFrom());
            verifyNoInteractions(mockTypeRateInMapper);
        }
    }

    @Nested
    @DisplayName("When Add Type Rate")
    class WhenAddTypeRate {

        @Test
        @DisplayName("Should Return Mapped Type Rate Response")
        void shouldReturnMappedTypeRateResponse() {
            var request = typeRateRequest();
            var config = receivableTypeConfiguration();
            var response = typeRateResponse();
            when(mockUseCase.addTypeRate(request.type(), request.spread(),
                    request.strategyKey(), request.validFrom())).thenReturn(config);
            when(mockTypeRateInMapper.from(config)).thenReturn(response);

            var result = controller.addTypeRate(request);

            assertThat(result).hasValueSatisfying(actual -> assertThat(actual)
                    .as("Mapped type rate response must contain all expected fields")
                    .usingRecursiveComparison()
                    .isEqualTo(typeRateResponse()));
            verify(mockUseCase).addTypeRate(request.type(), request.spread(),
                    request.strategyKey(), request.validFrom());
            verify(mockTypeRateInMapper).from(argThat(actual -> matchesAllFields(actual, receivableTypeConfiguration())));
        }

        @Test
        @DisplayName("Use Case Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenUseCaseReturnsNull() {
            var request = typeRateRequest();
            when(mockUseCase.addTypeRate(request.type(), request.spread(),
                    request.strategyKey(), request.validFrom())).thenReturn(null);

            assertThat(controller.addTypeRate(request)).isEmpty();

            verify(mockUseCase).addTypeRate(request.type(), request.spread(),
                    request.strategyKey(), request.validFrom());
            verifyNoInteractions(mockTypeRateInMapper);
        }

        @Test
        @DisplayName("Response Mapper Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenResponseMapperReturnsNull() {
            var request = typeRateRequest();
            var config = receivableTypeConfiguration();
            when(mockUseCase.addTypeRate(request.type(), request.spread(),
                    request.strategyKey(), request.validFrom())).thenReturn(config);
            when(mockTypeRateInMapper.from(config)).thenReturn(null);

            assertThat(controller.addTypeRate(request)).isEmpty();

            verify(mockUseCase).addTypeRate(request.type(), request.spread(),
                    request.strategyKey(), request.validFrom());
            verify(mockTypeRateInMapper).from(argThat(actual -> matchesAllFields(actual, receivableTypeConfiguration())));
        }

        @Test
        @DisplayName("Use Case Throws Exception Then Should Propagate Exception")
        void shouldPropagateUseCaseException() {
            var request = typeRateRequest();
            var exception = new IllegalStateException("Strategy not found");
            when(mockUseCase.addTypeRate(request.type(), request.spread(),
                    request.strategyKey(), request.validFrom())).thenThrow(exception);

            assertThatThrownBy(() -> controller.addTypeRate(request)).isSameAs(exception);

            verify(mockUseCase).addTypeRate(request.type(), request.spread(),
                    request.strategyKey(), request.validFrom());
            verifyNoInteractions(mockTypeRateInMapper);
        }
    }

    @Nested
    @DisplayName("When Find Receivable Type Configuration")
    class WhenFindReceivableTypeConfiguration {

        @Test
        @DisplayName("Should Return Mapped Type Rate Response")
        void shouldReturnMappedTypeRateResponse() {
            var type = ReceivableType.DUPLICATA_MERCANTIL;
            var at = LocalDateTime.of(2026, 9, 14, 10, 0);
            var config = receivableTypeConfiguration();
            var response = typeRateResponse();
            when(mockUseCase.findReceivableTypeConfiguration(type, at)).thenReturn(config);
            when(mockTypeRateInMapper.from(config)).thenReturn(response);

            var result = controller.findReceivableTypeConfiguration(type, at);

            assertThat(result)
                    .as("Mapped type rate response must contain all expected fields")
                    .usingRecursiveComparison()
                    .isEqualTo(typeRateResponse());
            verify(mockUseCase).findReceivableTypeConfiguration(type, at);
            verify(mockTypeRateInMapper).from(argThat(actual -> matchesAllFields(actual, receivableTypeConfiguration())));
        }

        @Test
        @DisplayName("Use Case Returns Null Then Should Return Mapped Null")
        void shouldReturnMappedNullWhenUseCaseReturnsNull() {
            var type = ReceivableType.DUPLICATA_MERCANTIL;
            var at = LocalDateTime.of(2026, 9, 14, 10, 0);
            when(mockUseCase.findReceivableTypeConfiguration(type, at)).thenReturn(null);
            when(mockTypeRateInMapper.from(null)).thenReturn(null);

            assertThat(controller.findReceivableTypeConfiguration(type, at)).isNull();

            verify(mockUseCase).findReceivableTypeConfiguration(type, at);
            verify(mockTypeRateInMapper).from(null);
        }

        @Test
        @DisplayName("Use Case Throws Exception Then Should Propagate Exception")
        void shouldPropagateUseCaseException() {
            var type = ReceivableType.DUPLICATA_MERCANTIL;
            var at = LocalDateTime.of(2026, 9, 14, 10, 0);
            var exception = new IllegalStateException("Receivable type configuration not found");
            when(mockUseCase.findReceivableTypeConfiguration(type, at)).thenThrow(exception);

            assertThatThrownBy(() -> controller.findReceivableTypeConfiguration(type, at)).isSameAs(exception);

            verify(mockUseCase).findReceivableTypeConfiguration(type, at);
            verifyNoInteractions(mockTypeRateInMapper);
        }
    }

    boolean matchesAllFields(Object actual, Object expected) {
        assertThat(actual)
                .as("All fields of %s must match", expected.getClass().getSimpleName())
                .usingRecursiveComparison()
                .isEqualTo(expected);
        return true;
    }

    BaseRateRequest baseRateRequest() {
        return new BaseRateRequest(Currency.BRL, new BigDecimal("0.01"),
                LocalDateTime.of(2026, 9, 14, 9, 0));
    }

    EffectiveRate effectiveRate() {
        return new EffectiveRate(new BigDecimal("0.01"),
                LocalDateTime.of(2026, 9, 14, 9, 0));
    }

    BaseRateResponse baseRateResponse() {
        return new BaseRateResponse(Currency.BRL, "0.01",
                LocalDateTime.of(2026, 9, 14, 9, 0));
    }

    TypeRateRequest typeRateRequest() {
        return new TypeRateRequest(ReceivableType.DUPLICATA_MERCANTIL, new BigDecimal("0.005"),
                "STANDARD", LocalDateTime.of(2026, 9, 14, 9, 0));
    }

    ReceivableTypeConfiguration receivableTypeConfiguration() {
        return new ReceivableTypeConfiguration(ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("0.005"), "STANDARD", LocalDateTime.of(2026, 9, 14, 9, 0));
    }

    TypeRateResponse typeRateResponse() {
        return new TypeRateResponse(ReceivableType.DUPLICATA_MERCANTIL, "0.005",
                "STANDARD", LocalDateTime.of(2026, 9, 14, 9, 0));
    }
}
