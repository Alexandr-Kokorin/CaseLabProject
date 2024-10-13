package caselab.controller;

import caselab.exception.entity.EntityNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@SuppressWarnings("MultipleStringLiterals")
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> notFoundException(EntityNotFoundException exception, Locale locale) {

        var problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            Objects.requireNonNull(messageSource.getMessage(exception.getMessage(),
                new Object[] {exception.getId()}, exception.getMessage(), locale
            ))
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(problemDetail);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException exception, Locale locale) {
        List<String> errorMessages = exception.getAllErrors().stream()
            .map(ObjectError::getDefaultMessage)
            .toList();

        return createProblemDetailResponseEntity(BAD_REQUEST, messageSource.getMessage(
                "errors.400.title", new Object[0], "errors.400.title", locale
            ), errorMessages, locale
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> unauthorizedException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    private ResponseEntity<ProblemDetail> createProblemDetailResponseEntity(
        HttpStatus status,
        String messageKey,
        Object errorDetails,
        Locale locale
    ) {

        String message = Objects.requireNonNull(messageSource.getMessage(
            messageKey, new Object[0], messageKey, locale
        ));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setProperty("errors", errorDetails);

        return ResponseEntity.status(status).body(problemDetail);
    }
}
