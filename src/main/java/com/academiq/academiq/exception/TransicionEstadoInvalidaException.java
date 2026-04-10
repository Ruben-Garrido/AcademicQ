package com.academiq.academiq.exception;

public class TransicionEstadoInvalidaException extends RuntimeException{
    public TransicionEstadoInvalidaException(String mensaje){
        super(mensaje);
    }
}
