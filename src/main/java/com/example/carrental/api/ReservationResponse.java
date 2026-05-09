package com.example.carrental.api;

import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Reservation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID reservationId,
        String carId,
        CarType carType,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.id(),
                reservation.car().id(),
                reservation.carType(),
                reservation.startDateTime(),
                reservation.endDateTime()
        );
    }
}
