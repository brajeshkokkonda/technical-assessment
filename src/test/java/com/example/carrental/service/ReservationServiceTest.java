package com.example.carrental.service;

import com.example.carrental.api.CarAvailabilityResponse;
import com.example.carrental.domain.Car;
import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Reservation;
import com.example.carrental.repository.CarInventory;
import com.example.carrental.repository.InMemoryReservationRepository;
import com.example.carrental.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceTest {
    private static final LocalDateTime MONDAY_10_AM = LocalDateTime.of(2026, 5, 11, 10, 0);

    private ReservationService reservationService;
    private CarInventory inventory;
    private ReservationRepository repository;

    @BeforeEach
    void setUp() {
        inventory = new CarInventory(Map.of(
                CarType.SEDAN, List.of(new Car("SEDAN-1", CarType.SEDAN), new Car("SEDAN-2", CarType.SEDAN)),
                CarType.SUV, List.of(new Car("SUV-1", CarType.SUV)),
                CarType.VAN, List.of(new Car("VAN-1", CarType.VAN))
        ));
        repository = new InMemoryReservationRepository();
        reservationService = new ReservationService(inventory, repository);
    }

    // ============ Basic Reservation Tests ============

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

    // ============ Null Parameter Tests ============

    @Test
    void rejectsNullCarType() {
        assertThatThrownBy(() -> reservationService.reserve(null, MONDAY_10_AM, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("carType is required");
    }

    @Test
    void rejectsNullStartDateTime() {
        assertThatThrownBy(() -> reservationService.reserve(CarType.SEDAN, null, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("startDateTime is required");
    }

    // ============ Duration Tests ============

    @Test
    void acceptsMinimumOneDayReservation() {
        Reservation reservation = reservationService.reserve(CarType.SEDAN, MONDAY_10_AM.plusDays(100), 1);

        assertThat(reservation.startDateTime()).isEqualTo(MONDAY_10_AM.plusDays(100));
        assertThat(reservation.endDateTime()).isEqualTo(MONDAY_10_AM.plusDays(101));
    }

    @Test
    void acceptsLongDurationReservation() {
        Reservation reservation = reservationService.reserve(CarType.SEDAN, MONDAY_10_AM.plusDays(200), 365);

        assertThat(reservation.endDateTime()).isEqualTo(MONDAY_10_AM.plusDays(565));
    }

    // ============ Overlapping Reservations Tests ============

    @Test
    void rejectsPartialOverlapAtStart() {
        LocalDateTime baseDate = MONDAY_10_AM.plusDays(1000);
        reservationService.reserve(CarType.SUV, baseDate, 3);
        // baseDate reserved for [baseDate, baseDate+3)

        // Try to reserve [baseDate+2, baseDate+4) - overlaps with existing
        assertThatThrownBy(() -> reservationService.reserve(CarType.SUV, baseDate.plusDays(2), 2))
                .isInstanceOf(NoCarsAvailableException.class);
    }

    @Test
    void rejectsPartialOverlapAtEnd() {
        LocalDateTime baseDate = MONDAY_10_AM.plusDays(2000);
        reservationService.reserve(CarType.SUV, baseDate, 3);
        // baseDate reserved for [baseDate, baseDate+3)

        // Try to reserve [baseDate-1, baseDate+1) - overlaps with existing
        assertThatThrownBy(() -> reservationService.reserve(CarType.SUV, baseDate.minusDays(1), 2))
                .isInstanceOf(NoCarsAvailableException.class);
    }

    @Test
    void rejectsFullOverlapContainedWithin() {
        LocalDateTime baseDate = MONDAY_10_AM.plusDays(3000);
        reservationService.reserve(CarType.SUV, baseDate, 5);
        // baseDate reserved for [baseDate, baseDate+5)

        // Try to reserve [baseDate+1, baseDate+3) - overlaps with existing
        assertThatThrownBy(() -> reservationService.reserve(CarType.SUV, baseDate.plusDays(1), 2))
                .isInstanceOf(NoCarsAvailableException.class);
    }

    @Test
    void rejectsFullOverlapContaining() {
        LocalDateTime baseDate = MONDAY_10_AM.plusDays(4000);
        Reservation first = reservationService.reserve(CarType.SUV, baseDate.plusDays(1), 2);
        // reserved for [baseDate+1, baseDate+3)

        // Try to reserve [baseDate, baseDate+5) - contains existing reservation
        assertThat(first.carType()).isEqualTo(CarType.SUV);
        assertThatThrownBy(() -> reservationService.reserve(CarType.SUV, baseDate, 5))
                .isInstanceOf(NoCarsAvailableException.class);
    }

    @Test
    void allowsReservationBeforeExistingReservation() {
        LocalDateTime baseDate = MONDAY_10_AM.plusDays(5000);
        reservationService.reserve(CarType.SEDAN, baseDate, 3);
        // baseDate reserved for [baseDate, baseDate+3)

        // Reserve [baseDate-3, baseDate) - no overlap
        Reservation before = reservationService.reserve(CarType.SEDAN, baseDate.minusDays(3), 3);

        assertThat(before.endDateTime()).isEqualTo(baseDate);
    }

    @Test
    void allowsReservationAfterExistingReservation() {
        LocalDateTime baseDate = MONDAY_10_AM.plusDays(6000);
        Reservation first = reservationService.reserve(CarType.SEDAN, baseDate, 3);
        // first reserved for [baseDate, baseDate+3)

        // Reserve [baseDate+4, baseDate+6) - after first
        Reservation after = reservationService.reserve(CarType.SEDAN, first.endDateTime().plusDays(1), 2);

        assertThat(after.startDateTime()).isEqualTo(first.endDateTime().plusDays(1));
    }

    // ============ Capacity Tests ============

    @Test
    void rejectsThirdReservationWhenOnlyTwoSedan() {
        LocalDateTime baseDate = MONDAY_10_AM.plusDays(7000);
        reservationService.reserve(CarType.SEDAN, baseDate, 2);
        reservationService.reserve(CarType.SEDAN, baseDate, 2);

        assertThatThrownBy(() -> reservationService.reserve(CarType.SEDAN, baseDate.plusDays(1), 2))
                .isInstanceOf(NoCarsAvailableException.class);
    }

    @Test
    void allowsThreeReservationsWithDifferentDatesAndTwoSedans() {
        LocalDateTime baseDate = MONDAY_10_AM.plusDays(8000);
        reservationService.reserve(CarType.SEDAN, baseDate, 2);
        reservationService.reserve(CarType.SEDAN, baseDate.plusDays(2), 2);
        reservationService.reserve(CarType.SEDAN, baseDate.plusDays(4), 2);

        assertThat(reservationService.reservations()).hasSize(3);
    }

    // ============ Car Availability Tests ============

    @Nested
    class CarAvailabilityTests {
        @Test
        void returnsAllCarTypesInAvailability() {
            List<CarAvailabilityResponse> availability = reservationService.getCarAvailability();

            assertThat(availability)
                    .hasSize(3)
                    .extracting(CarAvailabilityResponse::carType)
                    .contains(CarType.SEDAN, CarType.SUV, CarType.VAN);
        }

        @Test
        void returnsCorrectTotalCarsForEachType() {
            List<CarAvailabilityResponse> availability = reservationService.getCarAvailability();

            assertThat(availability)
                    .extracting(CarAvailabilityResponse::totalCars)
                    .contains(2, 1, 1);
        }

        @Test
        void showsAllAvailableWhenNoReservations() {
            List<CarAvailabilityResponse> availability = reservationService.getCarAvailability();

            assertThat(availability)
                    .anySatisfy(resp -> {
                        assertThat(resp.carType()).isEqualTo(CarType.SEDAN);
                        assertThat(resp.availableCars()).isEqualTo(2);
                        assertThat(resp.bookedCars()).isEqualTo(0);
                    })
                    .anySatisfy(resp -> {
                        assertThat(resp.carType()).isEqualTo(CarType.SUV);
                        assertThat(resp.availableCars()).isEqualTo(1);
                        assertThat(resp.bookedCars()).isEqualTo(0);
                    })
                    .anySatisfy(resp -> {
                        assertThat(resp.carType()).isEqualTo(CarType.VAN);
                        assertThat(resp.availableCars()).isEqualTo(1);
                        assertThat(resp.bookedCars()).isEqualTo(0);
                    });
        }

        @Test
        void calculatesAvailabilityAfterOneReservation() {
            LocalDateTime baseDate = MONDAY_10_AM.plusDays(9000);
            reservationService.reserve(CarType.SEDAN, baseDate, 2);

            List<CarAvailabilityResponse> availability = reservationService.getCarAvailability();

            CarAvailabilityResponse sedan = availability.stream()
                    .filter(r -> r.carType() == CarType.SEDAN)
                    .findFirst()
                    .orElse(null);

            assertThat(sedan)
                    .extracting(CarAvailabilityResponse::totalCars, CarAvailabilityResponse::bookedCars, CarAvailabilityResponse::availableCars)
                    .containsExactly(2, 1, 1);
        }

        @Test
        void multipleReservationsSameCarCountAsOne() {
            LocalDateTime baseDate = MONDAY_10_AM.plusDays(10000);
            Reservation first = reservationService.reserve(CarType.SEDAN, baseDate, 2);
            Reservation second = reservationService.reserve(CarType.SEDAN, first.endDateTime(), 2);

            assertThat(first.car().id()).isEqualTo(second.car().id());

            List<CarAvailabilityResponse> availability = reservationService.getCarAvailability();

            CarAvailabilityResponse sedan = availability.stream()
                    .filter(r -> r.carType() == CarType.SEDAN)
                    .findFirst()
                    .orElse(null);

            assertThat(sedan)
                    .hasFieldOrPropertyWithValue("bookedCars", 1)
                    .hasFieldOrPropertyWithValue("availableCars", 1);
        }
    }

    // ============ Reservation Retrieval Tests ============

    @Test
    void retrievesAllReservations() {
        LocalDateTime baseDate = MONDAY_10_AM.plusDays(11000);
        reservationService.reserve(CarType.SEDAN, baseDate, 1);
        reservationService.reserve(CarType.SUV, baseDate, 1);
        reservationService.reserve(CarType.VAN, baseDate, 1);

        assertThat(reservationService.reservations()).hasSize(3);
    }

    @Test
    void retrievesEmptyListWhenNoReservations() {
        CarInventory tempInventory = new CarInventory(Map.of(
                CarType.SEDAN, List.of(new Car("SEDAN-1", CarType.SEDAN))
        ));
        ReservationRepository tempRepository = new InMemoryReservationRepository();
        ReservationService tempService = new ReservationService(tempInventory, tempRepository);

        assertThat(tempService.reservations()).isEmpty();
    }

    // ============ Concurrent Reservation Tests ============

    @Test
    void assignsDifferentCarsForConcurrentReservations() {
        LocalDateTime baseDate = MONDAY_10_AM.plusDays(12000);
        Reservation first = reservationService.reserve(CarType.SEDAN, baseDate, 2);
        Reservation second = reservationService.reserve(CarType.SEDAN, baseDate, 2);
        Reservation third = reservationService.reserve(CarType.SEDAN, baseDate.plusDays(2), 2);

        assertThat(first.car().id()).isNotEqualTo(second.car().id());
        assertThat(third.car().id()).isEqualTo(first.car().id());
    }
}
