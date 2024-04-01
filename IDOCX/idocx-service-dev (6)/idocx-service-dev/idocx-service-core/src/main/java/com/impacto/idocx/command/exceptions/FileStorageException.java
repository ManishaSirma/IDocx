package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class FileStorageException extends RuntimeException {

    private final ErrorCode errorCode;
    public FileStorageException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
