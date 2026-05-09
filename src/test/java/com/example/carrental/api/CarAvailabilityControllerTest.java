package com.example.carrental.api;

import com.example.carrental.domain.CarType;
import com.example.carrental.service.ReservationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarAvailabilityControllerTest {

    @Test
    void returnsAvailabilityFromReservationService() {
        ReservationService service = new ReservationService(null, null) {
            @Override
            public List<CarAvailabilityResponse> getCarAvailability() {
                return List.of(new CarAvailabilityResponse(CarType.SEDAN, 2, 1, 1));
            }
        };
        CarController controller = new CarController(service);

        List<CarAvailabilityResponse> availability = controller.carAvailability();

        assertThat(availability).containsExactly(new CarAvailabilityResponse(CarType.SEDAN, 2, 1, 1));
    }
}
