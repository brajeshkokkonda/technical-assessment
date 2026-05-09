package com.example.carrental.api;

import com.example.carrental.domain.CarType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Information about the availability of cars by type")
public record CarAvailabilityResponse(
        @Schema(description = "The type of car (SEDAN, SUV, VAN)")
        CarType carType,
        @Schema(description = "Total number of cars of this type in inventory")
        int totalCars,
        @Schema(description = "Number of available cars of this type (not currently booked)")
        int availableCars,
        @Schema(description = "Number of cars of this type that are currently booked")
        int bookedCars
) {
}
