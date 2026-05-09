package com.example.carrental.domain;

import java.util.Objects;

public final class Car {
    private final String id;
    private final CarType type;

    public Car(String id, CarType type) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.type = Objects.requireNonNull(type, "type is required");
    }

    public String id() {
        return id;
    }

    public CarType type() {
        return type;
    }
}
