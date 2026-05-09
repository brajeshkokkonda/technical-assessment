package com.example.carrental.repository;

import com.example.carrental.domain.Car;
import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryReservationRepositoryTest {
    private InMemoryReservationRepository repository;
    private Reservation testReservation;
    private static final LocalDateTime START_TIME = LocalDateTime.of(2026, 5, 11, 10, 0);

    @BeforeEach
    void setUp() {
        repository = new InMemoryReservationRepository();
        Car car = new Car("SEDAN-1", CarType.SEDAN);
        testReservation = new Reservation(UUID.randomUUID(), car, START_TIME, 3);
    }

    @Test
    void savesReservation() {
        repository.save(testReservation);

        List<Reservation> all = repository.findAll();
        assertThat(all).hasSize(1).contains(testReservation);
    }

    @Test
    void savesMultipleReservations() {
        Reservation second = new Reservation(UUID.randomUUID(), new Car("SEDAN-2", CarType.SEDAN), START_TIME.plusDays(4), 2);

        repository.save(testReservation);
        repository.save(second);

        List<Reservation> all = repository.findAll();
        assertThat(all).hasSize(2).contains(testReservation, second);
    }

    @Test
    void findsByCarType() {
        Reservation suv = new Reservation(UUID.randomUUID(), new Car("SUV-1", CarType.SUV), START_TIME, 1);

        repository.save(testReservation);
        repository.save(suv);

        List<Reservation> sedans = repository.findByCarType(CarType.SEDAN);
        assertThat(sedans).hasSize(1).contains(testReservation);

        List<Reservation> suvs = repository.findByCarType(CarType.SUV);
        assertThat(suvs).hasSize(1).contains(suv);
    }

    @Test
    void returnsEmptyListWhenNoReservations() {
        List<Reservation> all = repository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    void returnsEmptyListWhenNoReservationsForCarType() {
        repository.save(testReservation);

        List<Reservation> suvs = repository.findByCarType(CarType.SUV);
        assertThat(suvs).isEmpty();
    }

    @Test
    void returnsImmutableList() {
        repository.save(testReservation);

        List<Reservation> all = repository.findAll();
        assertThat(all).isUnmodifiable();
    }

    @Test
    void findAllIsIndependentCopy() {
        repository.save(testReservation);

        List<Reservation> first = repository.findAll();
        Reservation second = new Reservation(UUID.randomUUID(), new Car("VAN-1", CarType.VAN), START_TIME, 1);
        repository.save(second);
        List<Reservation> second_call = repository.findAll();

        assertThat(first).hasSize(1);
        assertThat(second_call).hasSize(2);
    }

    @Test
    void findByCarTypeIsIndependentCopy() {
        repository.save(testReservation);

        List<Reservation> first = repository.findByCarType(CarType.SEDAN);
        Reservation second = new Reservation(UUID.randomUUID(), new Car("SEDAN-2", CarType.SEDAN), START_TIME.plusDays(4), 2);
        repository.save(second);
        List<Reservation> second_call = repository.findByCarType(CarType.SEDAN);

        assertThat(first).hasSize(1);
        assertThat(second_call).hasSize(2);
    }
}

