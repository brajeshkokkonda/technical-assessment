package com.example.carrental.service;

import com.example.carrental.domain.Car;
import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Reservation;
import com.example.carrental.repository.CarInventory;
import com.example.carrental.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReservationService {
    private final CarInventory carInventory;
    private final ReservationRepository reservationRepository;

    public ReservationService(CarInventory carInventory, ReservationRepository reservationRepository) {
        this.carInventory = carInventory;
        this.reservationRepository = reservationRepository;
    }

    public synchronized Reservation reserve(CarType carType, LocalDateTime startDateTime, int days) {
        Objects.requireNonNull(carType, "carType is required");
        Objects.requireNonNull(startDateTime, "startDateTime is required");
        if (days <= 0) {
            throw new IllegalArgumentException("Reservation must be at least one day");
        }

        LocalDateTime endDateTime = startDateTime.plusDays(days);
        List<Reservation> existingReservations = reservationRepository.findByCarType(carType);

        return carInventory.carsOfType(carType).stream()
                .filter(car -> isAvailable(car, existingReservations, startDateTime, endDateTime))
                .findFirst()
                .map(car -> createReservation(car, startDateTime, days))
                .orElseThrow(() -> new NoCarsAvailableException(carType, startDateTime, days));
    }

    public List<Reservation> reservations() {
        return reservationRepository.findAll();
    }

    private boolean isAvailable(
            Car car,
            List<Reservation> existingReservations,
            LocalDateTime requestedStart,
            LocalDateTime requestedEnd
    ) {
        return existingReservations.stream()
                .filter(reservation -> reservation.car().id().equals(car.id()))
                .noneMatch(reservation -> reservation.overlaps(requestedStart, requestedEnd));
    }

    private Reservation createReservation(Car car, LocalDateTime startDateTime, int days) {
        Reservation reservation = new Reservation(UUID.randomUUID(), car, startDateTime, days);
        reservationRepository.save(reservation);
        return reservation;
    }
}
