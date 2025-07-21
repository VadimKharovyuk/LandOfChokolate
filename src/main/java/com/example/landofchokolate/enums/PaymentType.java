package com.example.landofchokolate.enums;
import lombok.Getter;

@Getter
public enum PaymentType {
    QRCODE("Оплата по QR-коду"),
    IBAN("Оплата по IBAN"),
    PRIVATBANK("ПриватБанк (через Privat24)"),
    MONOBANK("MonoBank (по ссылке)"),
    CASH_ON_PICKUP("Оплата при получении (самовывоз)");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }
}
