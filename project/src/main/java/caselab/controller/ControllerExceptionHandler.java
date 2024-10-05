package caselab.controller;

import caselab.domain.entity.exception.ApiError;
import caselab.domain.entity.exception.ResourceNotFoundException;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFoundException(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> unauthorizedException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<?> notFoundException(HttpClientErrorException.NotFound e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ApiError error = new ApiError();
        error.setType("about:blank");
        error.setTitle("Ресурс не найден");
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setDetail(ex.getMessage());
        error.setInstance(request.getDescription(false));

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
