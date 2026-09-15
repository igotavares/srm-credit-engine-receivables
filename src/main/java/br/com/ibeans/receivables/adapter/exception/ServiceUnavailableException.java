package br.com.ibeans.receivables.adapter.exception;

public class ServiceUnavailableException extends InfrastructureException {

    public ServiceUnavailableException(String code, String title, String message) {
        super(code, title, message);
    }

}
