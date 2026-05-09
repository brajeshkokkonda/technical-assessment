package com.example.carrental.api;

import com.example.carrental.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public ReservationResponse reserve(@RequestBody ReservationRequest request) {
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
