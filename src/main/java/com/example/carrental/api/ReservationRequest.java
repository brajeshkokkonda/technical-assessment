package com.example.carrental.api;

import com.example.carrental.domain.CarType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequest(
        @NotNull CarType carType,
        @NotNull LocalDateTime startDateTime,
        @Min(1) @Schema(example = "3") int days
) {
}
