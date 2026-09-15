package br.com.ibeans.receivables.adapter.exception;

import lombok.Getter;

@Getter
public class InfrastructureException extends RuntimeException {

    private String code;
    private String title;

    public InfrastructureException(String code, String title, String message) {
        super(message);
        this.code = code;
        this.title = title;
    }

}
