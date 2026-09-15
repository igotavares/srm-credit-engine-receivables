package br.com.ibeans.receivables.application.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private String code;
    private String title;

    public BusinessException(String code, String title, String message) {
        super(message);
        this.code = code;
        this.title = title;
    }

}
