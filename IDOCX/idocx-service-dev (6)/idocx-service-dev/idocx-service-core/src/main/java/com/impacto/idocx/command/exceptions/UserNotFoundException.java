package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;
    public UserNotFoundException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
