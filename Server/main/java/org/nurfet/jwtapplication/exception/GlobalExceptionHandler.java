package org.nurfet.jwtapplication.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> NotFound(HttpServletRequest request, UserNotFoundException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError response = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                request.getRequestURL().toString(),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                        .withZone(ZoneId.systemDefault()).format(Instant.now()),
                e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Message", response));
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<String> handleBadCredentialsException(HttpServletRequest request, BadCredentialsException e) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleGeneralException(HttpServletRequest request, Exception e) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError response = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                request.getRequestURL().toString(),
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                        .withZone(ZoneId.systemDefault()).format(Instant.now()),
                e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("Message", response));
    }

//    @ExceptionHandler(AccessDeniedException.class)
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    public ResponseEntity<?> forbidden(HttpServletRequest request, AccessDeniedException e) {
//        HttpStatus status = HttpStatus.FORBIDDEN;
//        ApiError response = new ApiError(
//                status.value(),
//                status.getReasonPhrase(),
//                request.getRequestURL().toString(),
//                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
//                        .withZone(ZoneId.systemDefault()).format(Instant.now()),
//                e.getMessage());
//
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("Message", response));
//    }
}
