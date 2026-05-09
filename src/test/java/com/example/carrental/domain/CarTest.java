package com.example.carrental.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarTest {
    @Test
    void createsCar() {
        Car car = new Car("SEDAN-1", CarType.SEDAN);

        assertThat(car.id()).isEqualTo("SEDAN-1");
        assertThat(car.type()).isEqualTo(CarType.SEDAN);
    }

    @Test
    void rejectsNullId() {
        assertThatThrownBy(() -> new Car(null, CarType.SEDAN))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id is required");
    }

    @Test
    void rejectsNullType() {
        assertThatThrownBy(() -> new Car("SEDAN-1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("type is required");
    }

    @Test
    void createsCarWithAllCarTypes() {
        Car sedan = new Car("SEDAN-1", CarType.SEDAN);
        Car suv = new Car("SUV-1", CarType.SUV);
        Car van = new Car("VAN-1", CarType.VAN);

        assertThat(sedan.type()).isEqualTo(CarType.SEDAN);
        assertThat(suv.type()).isEqualTo(CarType.SUV);
        assertThat(van.type()).isEqualTo(CarType.VAN);
    }
}

