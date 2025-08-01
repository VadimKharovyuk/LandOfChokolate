package com.example.landofchokolate.enums;

import lombok.Getter;

import lombok.Getter;

@Getter
public enum DeliveryMethod {
    DELIVERY("Доставка"),
    PICKUP("Самовивіз"),
    NOVA_POSHTA("Нова Пошта");

    private final String description;

    DeliveryMethod(String description) {
        this.description = description;
    }
}