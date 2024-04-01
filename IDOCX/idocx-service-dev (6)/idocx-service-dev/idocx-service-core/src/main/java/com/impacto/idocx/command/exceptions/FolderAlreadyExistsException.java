package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class FolderAlreadyExistsException extends RuntimeException {

    private final ErrorCode errorCode;

    public FolderAlreadyExistsException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
