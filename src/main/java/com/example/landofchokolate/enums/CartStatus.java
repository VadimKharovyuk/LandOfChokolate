package com.example.landofchokolate.enums;


import lombok.Getter;

@Getter
public enum CartStatus {
    ACTIVE("Активная корзина"),
    ABANDONED("Заброшенная корзина"),
    CONVERTED("Оформлен в заказ"),
    EXPIRED("Истек срок действия");

    private final String displayName;

    CartStatus(String displayName) {
        this.displayName = displayName;
    }

}
