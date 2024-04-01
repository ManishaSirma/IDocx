package com.impacto.idocx.command.exceptions;

import com.impacto.idocx.command.common.GenericErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GenericErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<GenericErrorResponse> handleFileNotFoundException(FileNotFoundException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DirectoryCreationException.class)
    public ResponseEntity<GenericErrorResponse> handleDirectoryCreationException(DirectoryCreationException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(StorageInitializationException.class)
    public ResponseEntity<GenericErrorResponse> handleStorageInitializationException(StorageInitializationException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileReadingException.class)
    public ResponseEntity<GenericErrorResponse> handleFileReadingException(FileReadingException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FolderAlreadyExistsException.class)
    public ResponseEntity<GenericErrorResponse> handleFolderAlreadyExistsException(FolderAlreadyExistsException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<GenericErrorResponse> handleFileStorageException(FileStorageException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnsupportedException.class)
    public ResponseEntity<GenericErrorResponse> handleUnsupportedException(UnsupportedException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericErrorResponse> handleResourcesNotFoundException(ResourceNotFoundException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FailedToUpdateResourcesException.class)
    public ResponseEntity<GenericErrorResponse> handleFailedToUpdateResourcesException(FailedToUpdateResourcesException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FailedToCompressResourcesException.class)
    public ResponseEntity<GenericErrorResponse> handleFailedToCompressResourcesException(FailedToCompressResourcesException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidExcelFormatException.class)
    public ResponseEntity<GenericErrorResponse> handleInvalidExcelFormatException(InvalidExcelFormatException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(LimitExceedingException.class)
    public ResponseEntity<GenericErrorResponse> handleLimitExceedingException(LimitExceedingException ex) {
        return createResponseEntity(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private static ResponseEntity<GenericErrorResponse> createResponseEntity(ErrorCode errorCode, String message, HttpStatus status) {
        log.error("Timestamp: {}, Error handling: ErrorCode: {}, ErrorMessage: {}, HttpStatus: {}",
                LocalDateTime.now(), errorCode.getCode(), message, status);
        GenericErrorResponse errorResponse = new GenericErrorResponse(errorCode.getCode(), message);
        return new ResponseEntity<>(errorResponse, status);
    }
}

