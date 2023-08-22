package com.ares.urlshortening.exceptions;

public class ApiException extends RuntimeException {
    public ApiException(String s) {
        super(s);
    }
}
