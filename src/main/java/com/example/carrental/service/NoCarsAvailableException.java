package com.example.carrental.service;

import com.example.carrental.domain.CarType;

import java.time.LocalDateTime;

public class NoCarsAvailableException extends RuntimeException {
    public NoCarsAvailableException(CarType carType, LocalDateTime startDateTime, int days) {
        super("No " + carType + " cars available from " + startDateTime + " for " + days + " day(s)");
    }
}
