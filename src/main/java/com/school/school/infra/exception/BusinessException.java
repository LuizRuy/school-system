package com.school.school.infra.exception;

public class BusinessException extends  RuntimeException {
    public  BusinessException(String message) {
        super(message);
    }
}
