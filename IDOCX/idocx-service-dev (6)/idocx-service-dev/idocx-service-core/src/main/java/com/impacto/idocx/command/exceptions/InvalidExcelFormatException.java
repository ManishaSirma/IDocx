package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class InvalidExcelFormatException extends RuntimeException{
    private final ErrorCode errorCode;

    public InvalidExcelFormatException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
