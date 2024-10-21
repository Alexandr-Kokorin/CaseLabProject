package caselab.controller;

import caselab.exception.NotificationException;
import caselab.exception.UserExistsException;
import caselab.exception.VotingProcessIsOverException;
import caselab.exception.entity.EntityNotFoundException;
import caselab.exception.entity.VoteNotFoundException;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@SuppressWarnings("MultipleStringLiterals")
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> notFoundException(EntityNotFoundException exception, Locale locale) {
        return createProblemDetailResponse(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            new Object[] {exception.getId()},
            locale
        );
    }

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ProblemDetail> userExistsException(UserExistsException exception, Locale locale) {
        return createProblemDetailResponse(
            HttpStatus.CONFLICT,
            exception.getMessage(),
            new Object[] {exception.getEmail()},
            locale
        );
    }


    @ExceptionHandler(VotingProcessIsOverException.class)
    public ResponseEntity<ProblemDetail> votingProcessIsOverException(
        VotingProcessIsOverException exception,
        Locale locale
    ) {
        return createProblemDetailResponse(
            HttpStatus.CONFLICT,
            exception.getMessage(),
            new Object[] {exception.getId()},
            locale
        );
    }

    @ExceptionHandler(VoteNotFoundException.class)
    public ResponseEntity<ProblemDetail> voteNotFoundException(VoteNotFoundException exception, Locale locale) {
        return createProblemDetailResponse(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            new Object[] {exception.getEmail()},
            locale
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ProblemDetail> userUsernameNotFoundException(
        UsernameNotFoundException exception,
        Locale locale
    ) {
        return createProblemDetailResponse(
            HttpStatus.NOT_FOUND,
            "user.email.not_found",
            new Object[] {exception.getMessage()},
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

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ProblemDetail> notificationException(NotificationException exception, Locale locale) {
        return createProblemDetailResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            exception.getMessage(),
            new Object[0],
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
