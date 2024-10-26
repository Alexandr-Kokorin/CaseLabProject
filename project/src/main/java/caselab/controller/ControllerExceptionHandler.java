package caselab.controller;

import caselab.exception.document.version.DocumentPermissionAlreadyGrantedException;
import caselab.exception.base.ApplicationRuntimeException;
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

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ApplicationRuntimeException.class)
    public ResponseEntity<ProblemDetail> appRuntimeException(ApplicationRuntimeException exception, Locale locale) {
        return createProblemDetailResponse(
            exception.getHttpStatus(),
            exception.getMessage(),
            exception.getArgs(),
            locale
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException exception, Locale locale) {
        var problemDetail = createProblemDetail(
            HttpStatus.BAD_REQUEST,
            "errors.400.title",
            new Object[0],
            locale
        );
        problemDetail.setProperty("errors", exception.getAllErrors()
            .stream()
            .map(ObjectError::getDefaultMessage)
            .toList()
        );
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> unauthorizedException(BadCredentialsException e, Locale locale) {
        return createProblemDetailResponse(
            HttpStatus.UNAUTHORIZED,
            "user.unauthorized",
            new Object[] {e.getMessage()},
            locale
        );
    }

    @ExceptionHandler(DocumentPermissionAlreadyGrantedException.class)
    public ResponseEntity<ProblemDetail> documentPermissionAlreadyGrantedException(
        MissingDocumentPermissionException exception,
        Locale locale
    ) {
        return createProblemDetailResponse(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            new Object[] {exception.getPermissionName()},
            locale
        );
    }

    private ResponseEntity<ProblemDetail> createProblemDetailResponse(
        HttpStatus status,
        String messageKey,
        Object[] args,
        Locale locale
    ) {
        var problemDetail = createProblemDetail(status, messageKey, args, locale);
        return ResponseEntity.status(status).body(problemDetail);
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String messageKey, Object[] args, Locale locale) {
        return ProblemDetail.forStatusAndDetail(
            status,
            Objects.requireNonNull(
                messageSource.getMessage(messageKey, args, messageKey, locale)
            )
        );
    }
}
