package caselab.controller;

import caselab.domain.entity.exception.ApiError;
import caselab.domain.entity.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

@SuppressWarnings("MagicNumber")
@RestControllerAdvice(basePackages = {"caselab.controller"})
public class ControllerExceptionHandler {

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
