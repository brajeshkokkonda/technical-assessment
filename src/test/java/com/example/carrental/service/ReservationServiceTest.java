package com.example.carrental.service;

import com.example.carrental.domain.Car;
import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Reservation;
import com.example.carrental.repository.CarInventory;
import com.example.carrental.repository.InMemoryReservationRepository;
import com.example.carrental.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceTest {
    private static final LocalDateTime MONDAY_10_AM = LocalDateTime.of(2026, 5, 11, 10, 0);

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        CarInventory inventory = new CarInventory(Map.of(
                CarType.SEDAN, List.of(new Car("SEDAN-1", CarType.SEDAN), new Car("SEDAN-2", CarType.SEDAN)),
                CarType.SUV, List.of(new Car("SUV-1", CarType.SUV)),
                CarType.VAN, List.of(new Car("VAN-1", CarType.VAN))
        ));
        ReservationRepository repository = new InMemoryReservationRepository();
        reservationService = new ReservationService(inventory, repository);
    }

    @Test
    void reservesRequestedCarTypeForRequestedNumberOfDays() {
        Reservation reservation = reservationService.reserve(CarType.SEDAN, MONDAY_10_AM, 3);

        assertThat(reservation.carType()).isEqualTo(CarType.SEDAN);
        assertThat(reservation.startDateTime()).isEqualTo(MONDAY_10_AM);
        assertThat(reservation.endDateTime()).isEqualTo(MONDAY_10_AM.plusDays(3));
    }

    @Test
    void allowsReservationsUpToAvailableCapacityForSameTypeAndTime() {
        Reservation first = reservationService.reserve(CarType.SEDAN, MONDAY_10_AM, 2);
        Reservation second = reservationService.reserve(CarType.SEDAN, MONDAY_10_AM, 2);

        assertThat(first.car().id()).isNotEqualTo(second.car().id());
        assertThat(reservationService.reservations()).hasSize(2);
    }

    @Test
    void rejectsReservationWhenAllCarsOfRequestedTypeAreAlreadyBooked() {
        reservationService.reserve(CarType.SUV, MONDAY_10_AM, 2);

        assertThatThrownBy(() -> reservationService.reserve(CarType.SUV, MONDAY_10_AM.plusHours(1), 1))
                .isInstanceOf(NoCarsAvailableException.class)
                .hasMessageContaining("No SUV cars available");
    }

    @Test
    void allowsReservationForDifferentTypeAtSameTime() {
        reservationService.reserve(CarType.SUV, MONDAY_10_AM, 2);

        Reservation vanReservation = reservationService.reserve(CarType.VAN, MONDAY_10_AM, 2);

        assertThat(vanReservation.carType()).isEqualTo(CarType.VAN);
    }

    @Test
    void allowsBackToBackReservationsBecauseEndTimeIsExclusive() {
        Reservation first = reservationService.reserve(CarType.SUV, MONDAY_10_AM, 2);

        Reservation second = reservationService.reserve(CarType.SUV, first.endDateTime(), 1);

        assertThat(second.startDateTime()).isEqualTo(first.endDateTime());
    }

    @Test
    void rejectsZeroOrNegativeRentalDuration() {
        assertThatThrownBy(() -> reservationService.reserve(CarType.SEDAN, MONDAY_10_AM, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one day");

        assertThatThrownBy(() -> reservationService.reserve(CarType.SEDAN, MONDAY_10_AM, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one day");
    }
}
