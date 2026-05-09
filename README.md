# Car Rental Assessment

This is a small Spring Boot simulation of a car rental reservation system. It models a fixed inventory of Sedan, SUV, and Van cars, then accepts reservations for a requested type, start date/time, and duration in days.

## Design

- `CarType` defines the three supported types: `SEDAN`, `SUV`, and `VAN`.
- `CarInventory` owns the finite fleet. The default demo fleet has 2 Sedans, 1 SUV, and 1 Van.
- `ReservationService` contains the main business rule: find one car of the requested type that has no overlapping reservation.
- `Reservation` stores an exclusive end time, so a car returned at 10:00 can be booked again at exactly 10:00.
- `InMemoryReservationRepository` keeps the exercise lightweight. A production version would replace this with a database-backed repository and transactional locking.

## API

Start the application with Maven:

```bash
mvn spring-boot:run
```

Create a reservation:

```bash
curl -X POST http://localhost:8080/reservations \
  -H 'Content-Type: application/json' \
  -d '{"carType":"SEDAN","startDateTime":"2026-05-11T10:00:00","days":3}'
```

List reservations:

```bash
curl http://localhost:8080/reservations
```

Run tests:

```bash
mvn test
```

## Test Coverage

The unit tests prove that the system:

- reserves a car of the requested type for the requested number of days
- allows concurrent reservations only up to the available inventory count
- rejects requests when all cars of the requested type are booked for overlapping dates
- treats different car types independently
- allows back-to-back reservations
- rejects zero or negative rental durations

## AI Use Discussion Notes

Initial prompt example:

```text
Implement a car rental reservation system in Spring Boot.
```

Improved prompt example:

```text
Implement a Spring Boot car rental simulation using OOP. Keep domain logic independent from REST. Model Sedan, SUV, and Van inventory with limited counts. A reservation should request car type, LocalDateTime start, and number of days. Use exclusive end-time overlap checks. Add focused JUnit tests for capacity, overlap, different types, back-to-back bookings, and invalid durations.
```

How I would validate AI output:

- Read the domain model first and confirm the overlap rule is correct.
- Check edge cases: exact end/start boundary, partial overlaps, different car types, and duration validation.
- Run the unit tests and add missing cases before trusting the implementation.
- Review whether business logic lives in the service/domain layer rather than the controller.

Known gaps:

- Data is in memory and resets when the app restarts.
- There is no customer/account model, pricing, cancellation, payment, or car pickup location.
- The current synchronization is acceptable for this exercise, but production should use database transactions and locking.
- Request validation could be expanded with Bean Validation annotations and structured error responses.
