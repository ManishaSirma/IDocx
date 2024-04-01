package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class FileNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;
    public FileNotFoundException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
