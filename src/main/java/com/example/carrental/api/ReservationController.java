package com.example.carrental.api;

import com.example.carrental.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(@Valid @RequestBody ReservationRequest request) {
        return ReservationResponse.from(
                reservationService.reserve(request.carType(), request.startDateTime(), request.days())
        );
    }

    @GetMapping
    public List<ReservationResponse> reservations() {
        return reservationService.reservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }
}

@RestController
@RequestMapping("/cars")
class CarController {
    private final ReservationService reservationService;

    CarController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/availability")
    @Operation(summary = "Get car availability by type", description = "Returns the availability status of all car types, including total inventory, available cars, and booked cars")
    public List<CarAvailabilityResponse> carAvailability() {
        return reservationService.getCarAvailability();
    }
}
