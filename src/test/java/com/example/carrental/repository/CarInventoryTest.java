package com.example.carrental.repository;

import com.example.carrental.domain.Car;
import com.example.carrental.domain.CarType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CarInventoryTest {
    @Test
    void createsDefaultInventory() {
        CarInventory inventory = new CarInventory();

        assertThat(inventory.carsOfType(CarType.SEDAN)).hasSize(2);
        assertThat(inventory.carsOfType(CarType.SUV)).hasSize(1);
        assertThat(inventory.carsOfType(CarType.VAN)).hasSize(1);
    }

    @Test
    void createsCustomInventory() {
        Map<CarType, List<Car>> custom = Map.of(
                CarType.SEDAN, List.of(new Car("S-1", CarType.SEDAN), new Car("S-2", CarType.SEDAN), new Car("S-3", CarType.SEDAN)),
                CarType.SUV, List.of(new Car("U-1", CarType.SUV), new Car("U-2", CarType.SUV)),
                CarType.VAN, List.of(new Car("V-1", CarType.VAN))
        );

        CarInventory inventory = new CarInventory(custom);

        assertThat(inventory.carsOfType(CarType.SEDAN)).hasSize(3);
        assertThat(inventory.carsOfType(CarType.SUV)).hasSize(2);
        assertThat(inventory.carsOfType(CarType.VAN)).hasSize(1);
    }

    @Test
    void returnsEmptyListForUnknownCarType() {
        CarInventory inventory = new CarInventory(Map.of(
                CarType.SEDAN, List.of(new Car("S-1", CarType.SEDAN))
        ));

        assertThat(inventory.carsOfType(CarType.SUV)).isEmpty();
        assertThat(inventory.carsOfType(CarType.VAN)).isEmpty();
    }

    @Test
    void returnsCopyOfCarList() {
        CarInventory inventory = new CarInventory();

        List<Car> sedans = inventory.carsOfType(CarType.SEDAN);
        assertThat(sedans)
                .hasSize(2)
                .extracting(Car::id)
                .contains("SEDAN-1", "SEDAN-2");
    }

    @Test
    void hasCorrectCarTypes() {
        CarInventory inventory = new CarInventory();

        List<Car> sedans = inventory.carsOfType(CarType.SEDAN);
        List<Car> suvs = inventory.carsOfType(CarType.SUV);
        List<Car> vans = inventory.carsOfType(CarType.VAN);

        assertThat(sedans)
                .allMatch(car -> car.type() == CarType.SEDAN);
        assertThat(suvs)
                .allMatch(car -> car.type() == CarType.SUV);
        assertThat(vans)
                .allMatch(car -> car.type() == CarType.VAN);
    }

    @Test
    void inventoryIsImmutable() {
        Car sedan1 = new Car("S-1", CarType.SEDAN);
        Car sedan2 = new Car("S-2", CarType.SEDAN);

        Map<CarType, List<Car>> custom = Map.of(
                CarType.SEDAN, List.of(sedan1, sedan2)
        );

        CarInventory inventory = new CarInventory(custom);

        // Try to modify original map - should not affect inventory
        Map<CarType, List<Car>> custom2 = Map.of(
                CarType.SUV, List.of(new Car("U-1", CarType.SUV))
        );

        // Inventory should still have original cars
        assertThat(inventory.carsOfType(CarType.SEDAN)).hasSize(2);
    }

    @Test
    void carsOfTypeReturnsCorrectCars() {
        Car sedan1 = new Car("SEDAN-1", CarType.SEDAN);
        Car sedan2 = new Car("SEDAN-2", CarType.SEDAN);

        CarInventory inventory = new CarInventory(Map.of(
                CarType.SEDAN, List.of(sedan1, sedan2)
        ));

        List<Car> sedans = inventory.carsOfType(CarType.SEDAN);
        assertThat(sedans)
                .hasSize(2)
                .contains(sedan1, sedan2);
    }

    @Test
    void handlesEmptyCarType() {
        CarInventory inventory = new CarInventory(Map.of(
                CarType.SEDAN, List.of()
        ));

        assertThat(inventory.carsOfType(CarType.SEDAN)).isEmpty();
    }
}

