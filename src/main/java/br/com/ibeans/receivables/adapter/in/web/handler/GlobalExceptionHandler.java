package br.com.ibeans.receivables.adapter.in.web.handler;

import br.com.ibeans.receivables.adapter.exception.InfrastructureException;
import br.com.ibeans.receivables.adapter.exception.ServiceUnavailableException;
import br.com.ibeans.receivables.application.exception.BusinessException;
import br.com.ibeans.receivables.application.exception.NotFoundException;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final Tracer tracer;

    @ExceptionHandler(InfrastructureException.class)
    ProblemDetail infrastructure(InfrastructureException cause) {
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                cause.getCode(), cause.getTitle(), cause.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    ProblemDetail serviceUnavailable(ServiceUnavailableException cause) {
        return createProblemDetail(HttpStatus.SERVICE_UNAVAILABLE,
                cause.getCode(), cause.getTitle(), cause.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException cause) {
        return createProblemDetail(HttpStatus.NOT_FOUND,
                cause.getCode(), cause.getTitle(), cause.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    ProblemDetail business(BusinessException cause) {
        return createProblemDetail(HttpStatus.UNPROCESSABLE_CONTENT,
                cause.getCode(), cause.getTitle(), cause.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        var violations = exception.getBindingResult().getAllErrors().stream()
                .map(error -> new Violacao(
                        error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName(),
                        error.getCode(),
                        error.getDefaultMessage()
                ))
                .toList();

        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos.");
        problem.setTitle("Requisição inválida");
        problem.setProperty("violacoes", violations);
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException exception) {
        return genericProblem(HttpStatus.BAD_REQUEST,
                "Requisição inválida",
                exception.getMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class,
            OptimisticLockingFailureException.class})
    ProblemDetail conflict(Exception exception) {
        return genericProblem(HttpStatus.CONFLICT,
                "Conflito de dados",
                "A operação conflita com os dados existentes. Verifique os dados e tente novamente.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception exception) {
        logger.error("Erro inesperado ao processar a requisição", exception);
        return genericProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor",
                "Não foi possível processar a requisição. Tente novamente mais tarde.");
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(
            Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problem = body instanceof ProblemDetail detail ? detail : ProblemDetail.forStatus(status);
        problem.setProperty("code", "EXRA" + status.value());
        problem.setTitle(switch (status.value()) {
            case 400 -> "Requisição inválida";
            case 401 -> "Autenticação necessária";
            case 403 -> "Acesso negado";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método HTTP não permitido";
            case 406 -> "Formato de resposta não aceitável";
            case 409 -> "Conflito de dados";
            case 413 -> "Requisição muito grande";
            case 415 -> "Tipo de conteúdo não suportado";
            case 422 -> "Regra de negócio violada";
            case 429 -> "Limite de requisições excedido";
            case 500 -> "Erro interno do servidor";
            case 503 -> "Serviço indisponível";
            default -> "Erro ao processar a requisição";
        });
        if (status.is5xxServerError()) {
            problem.setDetail("Não foi possível processar a requisição. Tente novamente mais tarde.");
        }
        return super.createResponseEntity(withTraceId(problem), headers, status, request);
    }

    private ProblemDetail genericProblem(HttpStatus status, String title, String detail) {
        return createProblemDetail(status,
                "EXRA" + status.value(),
                title,
                detail);
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String code,
                                              String title, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty("code", code);
        problem.setTitle(title);
        return withTraceId(problem);
    }

    private ProblemDetail withTraceId(ProblemDetail problem) {
        var span = tracer.currentSpan();
        if (span != null) {
            problem.setProperty("traceId", span.context().traceId());
        }
        return problem;
    }

}
