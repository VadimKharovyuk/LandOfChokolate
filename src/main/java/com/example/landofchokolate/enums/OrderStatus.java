package com.example.landofchokolate.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    NEW("Нове замовлення"),
    PAID("Оплачено"),
    CANCELLED("Скасовано"),
    COMPLETED("Завершено"),
    READY_FOR_PICKUP("Готове до видачі в магазині");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}