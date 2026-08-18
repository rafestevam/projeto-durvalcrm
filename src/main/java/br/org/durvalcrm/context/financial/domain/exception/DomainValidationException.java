package br.org.durvalcrm.context.financial.domain.exception;

public class DomainValidationException extends RuntimeException {
    public DomainValidationException(String message){
        super(message);
    }
}
