package com.example.landofchokolate.enums;

import lombok.Getter;

@Getter
public enum SupportTopic {
    ORDER_ISSUE("Проблема із замовленням"),
    PAYMENT_QUESTION("Питання щодо оплати"),
    DELIVERY_DELAY("Затримка доставки"),
    PRODUCT_QUESTION("Питання про товар"),
    SITE_ERROR("Помилка на сайті"),
    OTHER("Інше");

    private final String description;

    SupportTopic(String description) {
        this.description = description;
    }
}