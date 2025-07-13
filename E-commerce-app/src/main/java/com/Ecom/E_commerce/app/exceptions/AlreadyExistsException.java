package com.Ecom.E_commerce.app.exceptions;

public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String s) {
        super(s);
    }
}
