package com.school.school.infra.exception;

public class EntityAlreadyExistsException extends  RuntimeException {
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}
