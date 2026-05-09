package com.example.carrental.api;

import com.example.carrental.OpenApiConfig;
import com.example.carrental.domain.Car;
import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Reservation;
import com.example.carrental.service.NoCarsAvailableException;
import com.example.carrental.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ReservationControllerIntegrationTest {
    private static final LocalDateTime START_TIME = LocalDateTime.of(2026, 5, 11, 10, 0);
    private static final Reservation RESERVATION = new Reservation(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            new Car("SEDAN-1", CarType.SEDAN),
            START_TIME,
            3
    );

    @Test
    void createsReservation() throws Exception {
        ReservationService service = new StubReservationService();

        standaloneSetup(new ReservationController(service))
                .build()
                .perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carType":"SEDAN","startDateTime":"2026-05-11T10:00:00","days":3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.carId").value("SEDAN-1"))
                .andExpect(jsonPath("$.carType").value("SEDAN"))
                .andExpect(jsonPath("$.startDateTime[0]").value(2026))
                .andExpect(jsonPath("$.startDateTime[1]").value(5))
                .andExpect(jsonPath("$.startDateTime[2]").value(11))
                .andExpect(jsonPath("$.startDateTime[3]").value(10))
                .andExpect(jsonPath("$.startDateTime[4]").value(0))
                .andExpect(jsonPath("$.endDateTime[0]").value(2026))
                .andExpect(jsonPath("$.endDateTime[1]").value(5))
                .andExpect(jsonPath("$.endDateTime[2]").value(14))
                .andExpect(jsonPath("$.endDateTime[3]").value(10))
                .andExpect(jsonPath("$.endDateTime[4]").value(0));
    }

    @Test
    void listsReservations() throws Exception {
        ReservationService service = new StubReservationService();

        standaloneSetup(new ReservationController(service))
                .build()
                .perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservationId").value("11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void mapsUnavailableCarsToConflict() throws Exception {
        ReservationService service = new ReservationService(null, null) {
            @Override
            public Reservation reserve(CarType carType, LocalDateTime startDateTime, int days) {
                throw new NoCarsAvailableException(carType, startDateTime, days);
            }
        };

        standaloneSetup(new ReservationController(service))
                .setControllerAdvice(new ReservationExceptionHandler())
                .build()
                .perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carType":"SUV","startDateTime":"2026-05-11T10:00:00","days":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("No SUV cars available from 2026-05-11T10:00 for 1 day(s)"));
    }

    @Test
    void mapsInvalidRequestsToBadRequest() throws Exception {
        ReservationService service = new ReservationService(null, null) {
            @Override
            public Reservation reserve(CarType carType, LocalDateTime startDateTime, int days) {
                throw new IllegalArgumentException("Reservation must be at least one day");
            }
        };

        standaloneSetup(new ReservationController(service))
                .setControllerAdvice(new ReservationExceptionHandler())
                .build()
                .perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carType":"SEDAN","startDateTime":"2026-05-11T10:00:00","days":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Reservation must be at least one day"));
    }

    @Test
    void createsOpenApiMetadata() {
        var openAPI = new OpenApiConfig().customOpenAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Car Rental API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0");
        assertThat(openAPI.getInfo().getDescription()).isEqualTo("API for managing car rental reservations");
    }

    private static class StubReservationService extends ReservationService {
        StubReservationService() {
            super(null, null);
        }

        @Override
        public Reservation reserve(CarType carType, LocalDateTime startDateTime, int days) {
            assertThat(carType).isEqualTo(CarType.SEDAN);
            assertThat(startDateTime).isEqualTo(START_TIME);
            assertThat(days).isEqualTo(3);
            return RESERVATION;
        }

        @Override
        public List<Reservation> reservations() {
            return List.of(RESERVATION);
        }
    }
}
