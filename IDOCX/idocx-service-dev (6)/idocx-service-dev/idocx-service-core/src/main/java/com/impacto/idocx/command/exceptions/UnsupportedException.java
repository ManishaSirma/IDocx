package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class UnsupportedException extends RuntimeException{
    private final ErrorCode errorCode;
    public UnsupportedException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
