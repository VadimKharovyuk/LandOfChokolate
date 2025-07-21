package com.example.landofchokolate.enums;
import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Ожидает оплаты"),
    PAID("Оплачено"),
    FAILED("Ошибка оплаты");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
}