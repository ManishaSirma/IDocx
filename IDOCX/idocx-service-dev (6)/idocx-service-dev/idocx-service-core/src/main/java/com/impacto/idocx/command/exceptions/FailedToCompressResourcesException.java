package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class FailedToCompressResourcesException extends RuntimeException {
    private final ErrorCode errorCode;

    public FailedToCompressResourcesException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
