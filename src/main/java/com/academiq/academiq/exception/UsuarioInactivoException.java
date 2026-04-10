package com.academiq.academiq.exception;

public class UsuarioInactivoException extends  RuntimeException{
    public UsuarioInactivoException (String mensaje){
        super(mensaje);
    }
}
