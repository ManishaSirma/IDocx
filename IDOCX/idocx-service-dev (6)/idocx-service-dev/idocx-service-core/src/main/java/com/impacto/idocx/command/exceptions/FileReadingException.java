package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class FileReadingException extends RuntimeException {

    private final ErrorCode errorCode;
    public FileReadingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
