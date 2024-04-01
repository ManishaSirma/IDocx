package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class LimitExceedingException extends RuntimeException {
    private final ErrorCode errorCode;

    public LimitExceedingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
