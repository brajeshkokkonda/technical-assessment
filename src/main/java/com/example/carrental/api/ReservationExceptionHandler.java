package com.example.carrental.api;

import com.example.carrental.service.NoCarsAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ReservationExceptionHandler {

    @ExceptionHandler(NoCarsAvailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> unavailable(NoCarsAvailableException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, NullPointerException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidRequest(RuntimeException exception) {
        return Map.of("error", exception.getMessage());
    }
}
