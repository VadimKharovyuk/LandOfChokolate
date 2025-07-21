package com.example.landofchokolate.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    NEW("Новый заказ"),
    PAID("Оплачен"),
    CANCELLED("Отменён"),
    COMPLETED("Завершён"),
    READY_FOR_PICKUP("Готов к выдаче в магазине");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}