package br.com.ibeans.receivables.adapter.in.web.receivable;

import br.com.ibeans.receivables.adapter.in.web.receivable.dto.CreateReceivableRequest;
import br.com.ibeans.receivables.adapter.in.web.receivable.dto.ReceivableResponse;
import br.com.ibeans.receivables.adapter.in.web.receivable.dto.UpdateReceivableRequest;
import br.com.ibeans.receivables.adapter.in.web.receivable.mapper.ReceivableInMapper;
import br.com.ibeans.receivables.application.port.in.receivable.CreateReceivableCommand;
import br.com.ibeans.receivables.application.port.in.receivable.ReceivableUseCases;
import br.com.ibeans.receivables.application.port.in.receivable.UpdateReceivableCommand;
import br.com.ibeans.receivables.domain.Currency;
import br.com.ibeans.receivables.domain.Receivable;
import br.com.ibeans.receivables.domain.ReceivableStatus;
import br.com.ibeans.receivables.domain.ReceivableType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceivableControllerTest {

    static final UUID RECEIVABLE_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Mock
    ReceivableUseCases mockUseCase;

    @Mock
    ReceivableInMapper mockMapper;

    @InjectMocks
    ReceivableController controller;

    @Nested
    @DisplayName("When Create")
    class WhenCreate {

        @Test
        @DisplayName("Should Return Mapped Receivable Response")
        void shouldReturnMappedReceivableResponse() {
            var request = createRequest();
            var command = createCommand();
            var receivable = receivable();
            var response = response();
            when(mockMapper.from(request)).thenReturn(command);
            when(mockUseCase.create(command)).thenReturn(receivable);
            when(mockMapper.from(receivable)).thenReturn(response);

            var result = controller.create(request);

            assertThat(result).hasValueSatisfying(actual -> assertThat(actual)
                    .as("Mapped receivable response must contain all expected fields")
                    .usingRecursiveComparison()
                    .isEqualTo(response()));
            verify(mockMapper).from(argThat((CreateReceivableRequest actual) -> matchesAllFields(actual, createRequest())));
            verify(mockUseCase).create(argThat((CreateReceivableCommand actual) -> matchesAllFields(actual, createCommand())));
            verify(mockMapper).from(argThat((Receivable actual) -> matchesAllFields(actual, receivable())));
        }

        @Test
        @DisplayName("Given Request Is Null Then Should Return Empty")
        void shouldReturnEmptyWhenRequestIsNull() {
            assertThat(controller.create(null)).isEmpty();

            verifyNoInteractions(mockMapper, mockUseCase);
        }

        @Test
        @DisplayName("Request Mapper Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenRequestMapperReturnsNull() {
            var request = createRequest();
            when(mockMapper.from(request)).thenReturn(null);

            assertThat(controller.create(request)).isEmpty();

            verify(mockMapper).from(argThat((CreateReceivableRequest actual) -> matchesAllFields(actual, createRequest())));
            verifyNoInteractions(mockUseCase);
        }

        @Test
        @DisplayName("Use Case Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenUseCaseReturnsNull() {
            var request = createRequest();
            var command = createCommand();
            when(mockMapper.from(request)).thenReturn(command);
            when(mockUseCase.create(command)).thenReturn(null);

            assertThat(controller.create(request)).isEmpty();

            verify(mockMapper).from(argThat((CreateReceivableRequest actual) -> matchesAllFields(actual, createRequest())));
            verify(mockUseCase).create(argThat((CreateReceivableCommand actual) -> matchesAllFields(actual, createCommand())));
        }

        @Test
        @DisplayName("Response Mapper Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenResponseMapperReturnsNull() {
            var request = createRequest();
            var command = createCommand();
            var receivable = receivable();
            when(mockMapper.from(request)).thenReturn(command);
            when(mockUseCase.create(command)).thenReturn(receivable);
            when(mockMapper.from(receivable)).thenReturn(null);

            assertThat(controller.create(request)).isEmpty();

            verify(mockMapper).from(argThat((CreateReceivableRequest actual) -> matchesAllFields(actual, createRequest())));
            verify(mockUseCase).create(argThat((CreateReceivableCommand actual) -> matchesAllFields(actual, createCommand())));
            verify(mockMapper).from(argThat((Receivable actual) -> matchesAllFields(actual, receivable())));
        }

        @Test
        @DisplayName("Use Case Throws Exception Then Should Propagate Exception")
        void shouldPropagateUseCaseException() {
            var request = createRequest();
            var command = createCommand();
            var exception = new IllegalStateException("Could not create receivable");
            when(mockMapper.from(request)).thenReturn(command);
            when(mockUseCase.create(command)).thenThrow(exception);

            assertThatThrownBy(() -> controller.create(request)).isSameAs(exception);

            verify(mockMapper).from(argThat((CreateReceivableRequest actual) -> matchesAllFields(actual, createRequest())));
            verify(mockUseCase).create(argThat((CreateReceivableCommand actual) -> matchesAllFields(actual, createCommand())));
        }
    }

    @Nested
    @DisplayName("When Update")
    class WhenUpdate {

        @Test
        @DisplayName("Should Return Mapped Receivable Response")
        void shouldReturnMappedReceivableResponse() {
            var request = updateRequest();
            var command = updateCommand();
            var receivable = updatedReceivable();
            var response = updatedResponse();
            when(mockMapper.from(request)).thenReturn(command);
            when(mockUseCase.update(RECEIVABLE_ID, command)).thenReturn(receivable);
            when(mockMapper.from(receivable)).thenReturn(response);

            var result = controller.update(RECEIVABLE_ID, request);

            assertThat(result).hasValueSatisfying(actual -> assertThat(actual)
                    .as("Mapped receivable response must contain all expected fields")
                    .usingRecursiveComparison()
                    .isEqualTo(updatedResponse()));
            verify(mockMapper).from(argThat((UpdateReceivableRequest actual) -> matchesAllFields(actual, updateRequest())));
            verify(mockUseCase).update(eq(RECEIVABLE_ID), argThat((UpdateReceivableCommand actual) -> matchesAllFields(actual, updateCommand())));
            verify(mockMapper).from(argThat((Receivable actual) -> matchesAllFields(actual, updatedReceivable())));
        }

        @Test
        @DisplayName("Given Request Is Null Then Should Return Empty")
        void shouldReturnEmptyWhenRequestIsNull() {
            assertThat(controller.update(RECEIVABLE_ID, null)).isEmpty();

            verifyNoInteractions(mockMapper, mockUseCase);
        }

        @Test
        @DisplayName("Request Mapper Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenRequestMapperReturnsNull() {
            var request = updateRequest();
            when(mockMapper.from(request)).thenReturn(null);

            assertThat(controller.update(RECEIVABLE_ID, request)).isEmpty();

            verify(mockMapper).from(argThat((UpdateReceivableRequest actual) -> matchesAllFields(actual, updateRequest())));
            verifyNoInteractions(mockUseCase);
        }

        @Test
        @DisplayName("Use Case Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenUseCaseReturnsNull() {
            var request = updateRequest();
            var command = updateCommand();
            when(mockMapper.from(request)).thenReturn(command);
            when(mockUseCase.update(RECEIVABLE_ID, command)).thenReturn(null);

            assertThat(controller.update(RECEIVABLE_ID, request)).isEmpty();

            verify(mockMapper).from(
                    argThat((UpdateReceivableRequest actual) ->
                            matchesAllFields(actual, updateRequest())));
            verify(mockUseCase).update(eq(RECEIVABLE_ID),
                    argThat((UpdateReceivableCommand actual) ->
                            matchesAllFields(actual, updateCommand())));
        }

        @Test
        @DisplayName("Response Mapper Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenResponseMapperReturnsNull() {
            var request = updateRequest();
            var command = updateCommand();
            var receivable = updatedReceivable();
            when(mockMapper.from(request)).thenReturn(command);
            when(mockUseCase.update(RECEIVABLE_ID, command)).thenReturn(receivable);
            when(mockMapper.from(receivable)).thenReturn(null);

            assertThat(controller.update(RECEIVABLE_ID, request)).isEmpty();

            verify(mockMapper).from(argThat((UpdateReceivableRequest actual) -> matchesAllFields(actual, updateRequest())));
            verify(mockUseCase).update(eq(RECEIVABLE_ID), argThat((UpdateReceivableCommand actual) -> matchesAllFields(actual, updateCommand())));
            verify(mockMapper).from(argThat((Receivable actual) -> matchesAllFields(actual, updatedReceivable())));
        }

        @Test
        @DisplayName("Use Case Throws Exception Then Should Propagate Exception")
        void shouldPropagateUseCaseException() {
            var request = updateRequest();
            var command = updateCommand();
            var exception = new IllegalStateException("Could not update receivable");
            when(mockMapper.from(request)).thenReturn(command);
            when(mockUseCase.update(RECEIVABLE_ID, command)).thenThrow(exception);

            assertThatThrownBy(() -> controller.update(RECEIVABLE_ID, request)).isSameAs(exception);

            verify(mockMapper).from(argThat((UpdateReceivableRequest actual) -> matchesAllFields(actual, updateRequest())));
            verify(mockUseCase).update(eq(RECEIVABLE_ID), argThat((UpdateReceivableCommand actual) -> matchesAllFields(actual, updateCommand())));
        }
    }

    @Nested
    @DisplayName("When Find")
    class WhenFind {

        @Test
        @DisplayName("Should Return Mapped Receivable Response")
        void shouldReturnMappedReceivableResponse() {
            var receivable = receivable();
            var response = response();
            when(mockUseCase.find(RECEIVABLE_ID)).thenReturn(receivable);
            when(mockMapper.from(receivable)).thenReturn(response);

            var result = controller.find(RECEIVABLE_ID);

            assertThat(result).hasValueSatisfying(actual -> assertThat(actual)
                    .as("Mapped receivable response must contain all expected fields")
                    .usingRecursiveComparison()
                    .isEqualTo(response()));
            verify(mockUseCase).find(RECEIVABLE_ID);
            verify(mockMapper).from(argThat((Receivable actual) -> matchesAllFields(actual, receivable())));
        }

        @Test
        @DisplayName("Given Id Is Null Then Should Return Empty")
        void shouldReturnEmptyWhenIdIsNull() {
            assertThat(controller.find(null)).isEmpty();

            verifyNoInteractions(mockMapper, mockUseCase);
        }

        @Test
        @DisplayName("Use Case Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenUseCaseReturnsNull() {
            when(mockUseCase.find(RECEIVABLE_ID)).thenReturn(null);

            assertThat(controller.find(RECEIVABLE_ID)).isEmpty();

            verify(mockUseCase).find(RECEIVABLE_ID);
            verifyNoInteractions(mockMapper);
        }

        @Test
        @DisplayName("Response Mapper Returns Null Then Should Return Empty")
        void shouldReturnEmptyWhenResponseMapperReturnsNull() {
            var receivable = receivable();
            when(mockUseCase.find(RECEIVABLE_ID)).thenReturn(receivable);
            when(mockMapper.from(receivable)).thenReturn(null);

            assertThat(controller.find(RECEIVABLE_ID)).isEmpty();

            verify(mockUseCase).find(RECEIVABLE_ID);
            verify(mockMapper).from(argThat((Receivable actual) -> matchesAllFields(actual, receivable())));
        }

        @Test
        @DisplayName("Use Case Throws Exception Then Should Propagate Exception")
        void shouldPropagateUseCaseException() {
            var exception = new IllegalStateException("Receivable not found");
            when(mockUseCase.find(RECEIVABLE_ID)).thenThrow(exception);

            assertThatThrownBy(() -> controller.find(RECEIVABLE_ID)).isSameAs(exception);

            verify(mockUseCase).find(RECEIVABLE_ID);
            verifyNoInteractions(mockMapper);
        }
    }

    boolean matchesAllFields(Object actual, Object expected) {
        assertThat(actual)
                .as("All fields of %s must match", expected.getClass().getSimpleName())
                .usingRecursiveComparison()
                .isEqualTo(expected);
        return true;
    }

    CreateReceivableRequest createRequest() {
        return new CreateReceivableRequest("CEDENTE-TESTE", new BigDecimal("1000.00"),
                Currency.BRL, LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 12, 14), ReceivableType.DUPLICATA_MERCANTIL);
    }

    CreateReceivableCommand createCommand() {
        return new CreateReceivableCommand("CEDENTE-TESTE", new BigDecimal("1000.00"),
                Currency.BRL, LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 12, 14), ReceivableType.DUPLICATA_MERCANTIL);
    }

    UpdateReceivableRequest updateRequest() {
        return new UpdateReceivableRequest(new BigDecimal("1800.75"),
                LocalDate.of(2027, 1, 14), ReceivableType.CHEQUE_PRE_DATADO);
    }

    UpdateReceivableCommand updateCommand() {
        return new UpdateReceivableCommand(new BigDecimal("1800.75"),
                LocalDate.of(2027, 1, 14), ReceivableType.CHEQUE_PRE_DATADO);
    }

    Receivable receivable() {
        return new Receivable(RECEIVABLE_ID, "CEDENTE-TESTE", new BigDecimal("1000.00"),
                Currency.BRL, LocalDate.of(2026, 9, 14), LocalDate.of(2026, 12, 14),
                ReceivableType.DUPLICATA_MERCANTIL, ReceivableStatus.PENDING, 0L,
                LocalDateTime.of(2026, 9, 14, 10, 0),
                LocalDateTime.of(2026, 9, 14, 10, 0));
    }

    Receivable updatedReceivable() {
        return new Receivable(RECEIVABLE_ID, "CEDENTE-TESTE", new BigDecimal("1800.75"),
                Currency.BRL, LocalDate.of(2026, 9, 14), LocalDate.of(2027, 1, 14),
                ReceivableType.CHEQUE_PRE_DATADO, ReceivableStatus.PENDING, 1L,
                LocalDateTime.of(2026, 9, 14, 10, 0),
                LocalDateTime.of(2026, 9, 14, 11, 0));
    }

    ReceivableResponse response() {
        return new ReceivableResponse(RECEIVABLE_ID, "CEDENTE-TESTE", "1000.00",
                Currency.BRL, LocalDate.of(2026, 9, 14), LocalDate.of(2026, 12, 14),
                ReceivableType.DUPLICATA_MERCANTIL, ReceivableStatus.PENDING, 0L,
                LocalDateTime.of(2026, 9, 14, 10, 0),
                LocalDateTime.of(2026, 9, 14, 10, 0));
    }

    ReceivableResponse updatedResponse() {
        return new ReceivableResponse(RECEIVABLE_ID, "CEDENTE-TESTE", "1800.75",
                Currency.BRL, LocalDate.of(2026, 9, 14), LocalDate.of(2027, 1, 14),
                ReceivableType.CHEQUE_PRE_DATADO, ReceivableStatus.PENDING, 1L,
                LocalDateTime.of(2026, 9, 14, 10, 0),
                LocalDateTime.of(2026, 9, 14, 11, 0));
    }
}
