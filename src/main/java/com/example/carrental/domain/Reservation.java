package com.example.carrental.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Reservation {
    private final UUID id;
    private final Car car;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;

    public Reservation(UUID id, Car car, LocalDateTime startDateTime, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Reservation must be at least one day");
        }
        this.id = Objects.requireNonNull(id, "id is required");
        this.car = Objects.requireNonNull(car, "car is required");
        this.startDateTime = Objects.requireNonNull(startDateTime, "startDateTime is required");
        this.endDateTime = startDateTime.plusDays(days);
    }

    public UUID id() {
        return id;
    }

    public Car car() {
        return car;
    }

    public CarType carType() {
        return car.type();
    }

    public LocalDateTime startDateTime() {
        return startDateTime;
    }

    public LocalDateTime endDateTime() {
        return endDateTime;
    }

    public boolean overlaps(LocalDateTime requestedStart, LocalDateTime requestedEnd) {
        return startDateTime.isBefore(requestedEnd) && requestedStart.isBefore(endDateTime);
    }
}
