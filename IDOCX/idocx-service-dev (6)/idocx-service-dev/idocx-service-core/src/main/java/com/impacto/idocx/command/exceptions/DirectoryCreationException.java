package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class DirectoryCreationException extends RuntimeException {

    private final ErrorCode errorCode;
    public DirectoryCreationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
