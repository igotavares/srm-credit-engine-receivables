package br.com.ibeans.receivables.adapter.in.web.handler;

import br.com.ibeans.receivables.adapter.exception.InfrastructureException;
import br.com.ibeans.receivables.adapter.exception.ServiceUnavailableException;
import br.com.ibeans.receivables.application.exception.BusinessException;
import br.com.ibeans.receivables.application.exception.NotFoundException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final Tracer tracer = mock(Tracer.class);
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(tracer);
    private static final String TRACE_ID = "0123456789abcdef0123456789abcdef";

    @Test
    void businessErrorPreservesCodeAndTitleAndIncludesActiveTrace() {
        activeTrace();
        var problem = handler.business(new BusinessException("MOEDAS_IGUAIS", "Moedas iguais", "Informe moedas diferentes"));

        assertThat(problem.getStatus()).isEqualTo(422);
        assertThat(problem.getTitle()).isEqualTo("Moedas iguais");
        assertThat(problem.getDetail()).isEqualTo("Informe moedas diferentes");
        assertThat(problem.getProperties()).containsEntry("code", "MOEDAS_IGUAIS")
                .containsEntry("traceId", TRACE_ID);
    }

    @Test
    void frameworkErrorIncludesActiveTraceAndPreservesHeaders() {
        activeTrace();
        var headers = new HttpHeaders();
        headers.add("Allow", "GET");
        var response = handler.createResponseEntity(ProblemDetail.forStatus(405), headers,
                HttpStatus.METHOD_NOT_ALLOWED, mock(WebRequest.class));

        assertThat(response.getHeaders().getFirst("Allow")).isEqualTo("GET");
        assertThat(((ProblemDetail) response.getBody()).getProperties())
                .containsEntry("code", "EXRA405").containsEntry("traceId", TRACE_ID);
    }

    @Test
    void genericErrorIncludesActiveTrace() {
        activeTrace();
        var problem = handler.badRequest(new IllegalArgumentException("Inválido"));
        assertProblem(problem, 400, "EXRA400", "Requisição inválida", "Inválido");
        assertThat(problem.getProperties())
                .containsEntry("code", "EXRA400").containsEntry("traceId", TRACE_ID);
    }

    @Test
    void missingSpanDoesNotInventTraceId() {
        assertThat(handler.badRequest(new IllegalArgumentException("Inválido")).getProperties())
                .containsEntry("code", "EXRA400").doesNotContainKey("traceId");
    }

    @Test
    void infrastructureErrorPreservesCustomProblemFields() {
        activeTrace();
        var problem = handler.infrastructure(new InfrastructureException("INFRA001", "Falha de integração", "Falha ao consultar provedor"));

        assertProblem(problem, 500, "INFRA001", "Falha de integração", "Falha ao consultar provedor");
        assertThat(problem.getProperties()).containsEntry("traceId", TRACE_ID);
    }

    @Test
    void serviceUnavailablePreservesCustomProblemFields() {
        activeTrace();
        var problem = handler.serviceUnavailable(new ServiceUnavailableException("INFRA503", "Provedor indisponível", "Tente novamente"));

        assertProblem(problem, 503, "INFRA503", "Provedor indisponível", "Tente novamente");
        assertThat(problem.getProperties()).containsEntry("traceId", TRACE_ID);
    }

    @Test
    void notFoundPreservesDomainCodeAndMessage() {
        activeTrace();
        var problem = handler.notFound(new NotFoundException("EXRA001", "Cotação não encontrada"));

        assertProblem(problem, 404, "EXRA001", "Recurso não encontrado", "Cotação não encontrada");
        assertThat(problem.getProperties()).containsEntry("traceId", TRACE_ID);
    }

    @ParameterizedTest
    @MethodSource("conflicts")
    void conflictReturnsSafeDetail(Exception exception) {
        activeTrace();
        var problem = handler.conflict(exception);

        assertProblem(problem, 409, "EXRA409", "Conflito de dados",
                "A operação conflita com os dados existentes. Verifique os dados e tente novamente.");
        assertThat(problem.getProperties()).containsEntry("traceId", TRACE_ID);
    }

    static Stream<Exception> conflicts() {
        return Stream.of(new DataIntegrityViolationException("SQL: internal constraint"),
                new OptimisticLockingFailureException("Internal entity version"));
    }

    @Test
    void unexpectedErrorDoesNotExposeExceptionMessage() {
        activeTrace();
        var problem = handler.unexpected(new RuntimeException("Internal database information"));

        assertProblem(problem, 500, "EXRA500", "Erro interno do servidor",
                "Não foi possível processar a requisição. Tente novamente mais tarde.");
        assertThat(problem.getProperties()).containsEntry("traceId", TRACE_ID);
    }

    @Test
    void validationIncludesFieldAndObjectViolations() {
        activeTrace();
        var binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "rate", null, false,
                new String[]{"NotNull"}, null, "não deve ser nulo"));
        binding.addError(new ObjectError("request", new String[]{"DifferentCurrencies"},
                null, "As moedas devem ser diferentes"));
        var exception = new MethodArgumentNotValidException(mock(MethodParameter.class), binding);
        var headers = new HttpHeaders();
        headers.add("X-Test", "validation");

        var response = handler.handleMethodArgumentNotValid(exception, headers,
                HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getFirst("X-Test")).isEqualTo("validation");
        var problem = (ProblemDetail) response.getBody();
        assertProblem(problem, 400, "EXRA400", "Requisição inválida", "Um ou mais campos estão inválidos.");
        assertThat(problem.getProperties()).containsEntry("traceId", TRACE_ID)
                .containsEntry("violacoes", List.of(
                        new Violacao("rate", "NotNull", "não deve ser nulo"),
                        new Violacao("request", "DifferentCurrencies", "As moedas devem ser diferentes")));
    }

    @ParameterizedTest
    @CsvSource({
            "400, Requisição inválida", "401, Autenticação necessária",
            "403, Acesso negado", "404, Recurso não encontrado",
            "405, Método HTTP não permitido", "406, Formato de resposta não aceitável",
            "409, Conflito de dados", "413, Requisição muito grande",
            "415, Tipo de conteúdo não suportado", "422, Regra de negócio violada",
            "429, Limite de requisições excedido", "500, Erro interno do servidor",
            "503, Serviço indisponível", "418, Erro ao processar a requisição",
            "502, Erro ao processar a requisição"
    })
    void frameworkErrorsStandardizeStatusCodeTitleAndDetail(int status, String title) {
        var original = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), "Original detail");
        original.setProperty("custom", "preserved");
        var response = handler.createResponseEntity(original, new HttpHeaders(),
                HttpStatusCode.valueOf(status), mock(WebRequest.class));

        assertThat(response.getStatusCode().value()).isEqualTo(status);
        var problem = (ProblemDetail) response.getBody();
        assertThat(problem).isSameAs(original);
        assertProblem(problem, status, "EXRA" + status, title, status >= 500
                ? "Não foi possível processar a requisição. Tente novamente mais tarde." : "Original detail");
        assertThat(problem.getProperties()).containsEntry("custom", "preserved").doesNotContainKey("traceId");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "Unexpected body")
    void frameworkCreatesProblemWhenBodyIsNotProblemDetail(String body) {
        var response = handler.createResponseEntity(body, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);
        var problem = (ProblemDetail) response.getBody();
        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getTitle()).isEqualTo("Requisição inválida");
        assertThat(problem.getProperties()).containsEntry("code", "EXRA400");
    }

    private static void assertProblem(ProblemDetail problem, int status, String code, String title, String detail) {
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(status);
        assertThat(problem.getTitle()).isEqualTo(title);
        assertThat(problem.getDetail()).isEqualTo(detail);
        assertThat(problem.getProperties()).containsEntry("code", code);
    }

    private void activeTrace() {
        var span = mock(Span.class);
        var context = mock(TraceContext.class);
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(context);
        when(context.traceId()).thenReturn(TRACE_ID);
    }
}
