package com.example.landofchokolate.enums;

import lombok.Getter;

/**
 * Единицы измерения для цены товара
 */
@Getter
public enum PriceUnit {
    PER_PIECE("ціна за штуку"),
    PER_100G("ціна за 100 г"),
    PER_KG("Ціна за 1 кг"),
    PER_LITER("ціна за літр"),
    PER_PORTION("ціна за порцію"),
    PER_BOX("ціна за коробку"),
    PER_PACK("ціна за упаковку");

    private final String label;

    PriceUnit(String label) {
        this.label = label;
    }


}