package com.example.carrental.repository;

import com.example.carrental.domain.Car;
import com.example.carrental.domain.CarType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CarInventory {
    private final Map<CarType, List<Car>> carsByType;

    public CarInventory() {
        this(Map.of(
                CarType.SEDAN, List.of(new Car("SEDAN-1", CarType.SEDAN), new Car("SEDAN-2", CarType.SEDAN)),
                CarType.SUV, List.of(new Car("SUV-1", CarType.SUV)),
                CarType.VAN, List.of(new Car("VAN-1", CarType.VAN))
        ));
    }

    public CarInventory(Map<CarType, List<Car>> carsByType) {
        this.carsByType = Map.copyOf(carsByType);
    }

    public List<Car> carsOfType(CarType type) {
        return carsByType.getOrDefault(type, List.of());
    }
}
