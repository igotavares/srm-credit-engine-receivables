package br.com.ibeans.receivables.application.exception;

public class BusinessConflictException extends BusinessException {
    public BusinessConflictException(String code, String title, String message) {
        super(code, title, message);
    }
}
