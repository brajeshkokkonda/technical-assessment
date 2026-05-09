package com.example.carrental.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {
    private static final LocalDateTime START_TIME = LocalDateTime.of(2026, 5, 11, 10, 0);
    private static final Car TEST_CAR = new Car("TEST-1", CarType.SEDAN);
    private static final UUID TEST_ID = UUID.randomUUID();

    @Test
    void createsReservationWithValidParameters() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 3);

        assertThat(reservation.id()).isEqualTo(TEST_ID);
        assertThat(reservation.car()).isEqualTo(TEST_CAR);
        assertThat(reservation.startDateTime()).isEqualTo(START_TIME);
        assertThat(reservation.endDateTime()).isEqualTo(START_TIME.plusDays(3));
    }

    @Test
    void calculatesEndTimeAsExclusive() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 1);

        assertThat(reservation.endDateTime()).isEqualTo(START_TIME.plusDays(1));
    }

    @Test
    void rejectsZeroDays() {
        assertThatThrownBy(() -> new Reservation(TEST_ID, TEST_CAR, START_TIME, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one day");
    }

    @Test
    void rejectsNegativeDays() {
        assertThatThrownBy(() -> new Reservation(TEST_ID, TEST_CAR, START_TIME, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one day");
    }

    @Test
    void rejectsNullId() {
        assertThatThrownBy(() -> new Reservation(null, TEST_CAR, START_TIME, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id is required");
    }

    @Test
    void rejectsNullCar() {
        assertThatThrownBy(() -> new Reservation(TEST_ID, null, START_TIME, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("car is required");
    }

    @Test
    void rejectsNullStartDateTime() {
        assertThatThrownBy(() -> new Reservation(TEST_ID, TEST_CAR, null, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("startDateTime is required");
    }

    @Test
    void returnsCarType() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 1);

        assertThat(reservation.carType()).isEqualTo(CarType.SEDAN);
    }

    // ============ Overlap Detection Tests ============

    @Test
    void detectsExactOverlapWithSameTimes() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 3);

        assertThat(reservation.overlaps(START_TIME, START_TIME.plusDays(3))).isTrue();
    }

    @Test
    void detectsPartialOverlapAtStart() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 3);
        LocalDateTime requestStart = START_TIME.minusDays(1);
        LocalDateTime requestEnd = START_TIME.plusDays(1);

        assertThat(reservation.overlaps(requestStart, requestEnd)).isTrue();
    }

    @Test
    void detectsPartialOverlapAtEnd() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 3);
        LocalDateTime requestStart = START_TIME.plusDays(2);
        LocalDateTime requestEnd = START_TIME.plusDays(4);

        assertThat(reservation.overlaps(requestStart, requestEnd)).isTrue();
    }

    @Test
    void detectsFullOverlapContainedWithin() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 5);
        LocalDateTime requestStart = START_TIME.plusDays(1);
        LocalDateTime requestEnd = START_TIME.plusDays(3);

        assertThat(reservation.overlaps(requestStart, requestEnd)).isTrue();
    }

    @Test
    void detectsFullOverlapContaining() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 2);
        LocalDateTime requestStart = START_TIME.minusDays(1);
        LocalDateTime requestEnd = START_TIME.plusDays(3);

        assertThat(reservation.overlaps(requestStart, requestEnd)).isTrue();
    }

    @Test
    void allowsBackToBackWithExclusiveEndTime() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 3);
        LocalDateTime requestStart = reservation.endDateTime();
        LocalDateTime requestEnd = requestStart.plusDays(2);

        assertThat(reservation.overlaps(requestStart, requestEnd)).isFalse();
    }

    @Test
    void allowsReservationBeforeWithoutOverlap() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 3);
        LocalDateTime requestStart = START_TIME.minusDays(5);
        LocalDateTime requestEnd = START_TIME;

        assertThat(reservation.overlaps(requestStart, requestEnd)).isFalse();
    }

    @Test
    void allowsReservationAfterWithoutOverlap() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 3);
        LocalDateTime requestStart = reservation.endDateTime();
        LocalDateTime requestEnd = requestStart.plusDays(3);

        assertThat(reservation.overlaps(requestStart, requestEnd)).isFalse();
    }

    @Test
    void detectsOverlapWhenRequestEndIsAtReservationStart() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME.plusDays(10), 3);
        LocalDateTime requestStart = START_TIME;
        LocalDateTime requestEnd = START_TIME.plusDays(10);

        // requestEnd equals reservationStart, so no overlap (exclusive end)
        assertThat(reservation.overlaps(requestStart, requestEnd)).isFalse();
    }

    @Test
    void detectsOverlapWhenReservationEndIsAtRequestStart() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 5);
        LocalDateTime requestStart = START_TIME.plusDays(5);
        LocalDateTime requestEnd = START_TIME.plusDays(10);

        // reservationEnd equals requestStart, so no overlap (exclusive end)
        assertThat(reservation.overlaps(requestStart, requestEnd)).isFalse();
    }

    @Test
    void acceptsMinimumOneDayDuration() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, 1);

        assertThat(reservation.startDateTime()).isEqualTo(START_TIME);
        assertThat(reservation.endDateTime()).isEqualTo(START_TIME.plusDays(1));
    }

    @Test
    void acceptsLargeDurationValues() {
        Reservation reservation = new Reservation(TEST_ID, TEST_CAR, START_TIME, Integer.MAX_VALUE);

        assertThat(reservation.endDateTime()).isEqualTo(START_TIME.plusDays(Integer.MAX_VALUE));
    }
}

