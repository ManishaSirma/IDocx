package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class StorageInitializationException extends RuntimeException{
    private final ErrorCode errorCode;
    public StorageInitializationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
