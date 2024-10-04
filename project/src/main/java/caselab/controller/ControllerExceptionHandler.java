package caselab.controller;

import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@SuppressWarnings("MagicNumber")
@RestControllerAdvice(basePackages = {"caselab.controller"})
public class ControllerExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<?> notFoundException(HttpClientErrorException.NotFound e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Optional.empty());
    }
}
