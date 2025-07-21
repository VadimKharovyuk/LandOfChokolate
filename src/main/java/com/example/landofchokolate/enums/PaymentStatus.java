package com.example.landofchokolate.enums;
import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Ожидает оплаты"),
    PROCESSING("В обработке"),
    COMPLETED("Завершено"),
    FAILED("Не удалось"),
    REFUNDED("Возвращено"),
    CANCELLED("Отменено");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
}