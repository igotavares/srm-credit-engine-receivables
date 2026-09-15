package br.com.ibeans.receivables.application.exception;

public class NotFoundException extends BusinessException {
    public NotFoundException(String code, String message) {
        super(code, "Recurso não encontrado", message);
    }
}
