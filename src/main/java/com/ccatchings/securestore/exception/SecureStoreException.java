package com.ccatchings.securestore.exception;

public class SecureStoreException extends Exception{

    private String message;

    public SecureStoreException(String exceptionMessage){
        this.message = exceptionMessage;
    }
}
