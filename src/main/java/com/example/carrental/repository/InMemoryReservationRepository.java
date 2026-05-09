package com.example.carrental.repository;

import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Reservation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InMemoryReservationRepository implements ReservationRepository {
    private final List<Reservation> reservations = new ArrayList<>();

    @Override
    public synchronized void save(Reservation reservation) {
        reservations.add(reservation);
    }

    @Override
    public synchronized List<Reservation> findByCarType(CarType carType) {
        return reservations.stream()
                .filter(reservation -> reservation.carType() == carType)
                .toList();
    }

    @Override
    public synchronized List<Reservation> findAll() {
        return List.copyOf(reservations);
    }
}
