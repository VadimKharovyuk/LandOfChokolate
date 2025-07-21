package com.example.landofchokolate.enums;

import lombok.Getter;

@Getter
public enum DeliveryMethod {
    DELIVERY("Доставка"),
    PICKUP("Самовывоз");

    private final String description;

    DeliveryMethod(String description) {
        this.description = description;
    }
}