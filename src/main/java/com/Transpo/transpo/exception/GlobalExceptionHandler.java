package com.Transpo.transpo.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, WebRequest req) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiError(404, "Not Found", ex.getMessage(), req.getDescription(false)));
}

@ExceptionHandler(BadRequestException.class)
public ResponseEntity<ApiError> handleBad(BadRequestException ex, WebRequest req) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiError(400, "Bad Request", ex.getMessage(), req.getDescription(false)));
}

@ExceptionHandler(ConflictException.class)
public ResponseEntity<ApiError> handleConflict(ConflictException ex, WebRequest req) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiError(409, "Conflict", ex.getMessage(), req.getDescription(false)));
}


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, WebRequest req) {
        ApiError err = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage(), req.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
