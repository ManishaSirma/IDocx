package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class FailedToDeleteResorceException extends RuntimeException {

    private final ErrorCode errorCode;
    public FailedToDeleteResorceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
