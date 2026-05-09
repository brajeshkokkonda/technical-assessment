package com.example.carrental.api;

import com.example.carrental.domain.CarType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidRequest() {
        ReservationRequest request = new ReservationRequest(
                CarType.SEDAN,
                LocalDateTime.of(2026, 5, 11, 10, 0),
                3
        );

        assertThat(request.carType()).isEqualTo(CarType.SEDAN);
        assertThat(request.startDateTime()).isEqualTo(LocalDateTime.of(2026, 5, 11, 10, 0));
        assertThat(request.days()).isEqualTo(3);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsMissingCarType() {
        ReservationRequest request = new ReservationRequest(null, LocalDateTime.of(2026, 5, 11, 10, 0), 3);

        assertThat(validator.validate(request))
                .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("carType"));
    }

    @Test
    void rejectsMissingStartDateTime() {
        ReservationRequest request = new ReservationRequest(CarType.SEDAN, null, 3);

        assertThat(validator.validate(request))
                .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("startDateTime"));
    }

    @Test
    void rejectsNonPositiveDays() {
        ReservationRequest request = new ReservationRequest(CarType.SEDAN, LocalDateTime.of(2026, 5, 11, 10, 0), 0);

        assertThat(validator.validate(request))
                .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("days"));
    }
}
