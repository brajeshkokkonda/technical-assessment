package com.example.carrental.api;

import com.example.carrental.domain.CarType;

import java.time.LocalDateTime;

public record ReservationRequest(
        CarType carType,
        LocalDateTime startDateTime,
        int days
) {
}
