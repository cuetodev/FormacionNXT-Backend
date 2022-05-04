package com.cuetodev.db1.Persona.application.errorhandling;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocesableException  extends RuntimeException {
    public UnprocesableException(String message) {
        super(message);
    }
}