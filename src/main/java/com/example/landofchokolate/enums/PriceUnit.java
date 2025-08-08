package com.example.landofchokolate.enums;

import lombok.Getter;

/**
 * Единицы измерения для цены товара
 */
@Getter
public enum PriceUnit {
    PER_PIECE("за штуку"),
    PER_100G("за 100 г"),
    PER_KG("за 1 кг"),
    PER_LITER("за літр"),
    PER_PORTION("за порцію"),
    PER_BOX("за коробку"),
    PER_PACK("за упаковку");

    private final String label;

    PriceUnit(String label) {
        this.label = label;
    }


}