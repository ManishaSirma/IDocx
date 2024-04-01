
package com.impacto.idocx.command.exceptions;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
