package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class FailedToUpdateResourcesException extends RuntimeException{
    private final ErrorCode errorCode;
    public FailedToUpdateResourcesException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
