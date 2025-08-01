package com.example.landofchokolate.dto.novaposhta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPrice {
    private BigDecimal cost;                    // Стоимость доставки
    private BigDecimal assessedCost;            // Оценочная стоимость
    private String costRedelivery;              // Стоимость обратной доставки
    private String costPack;                    // Стоимость упаковки
    private String costVolumeWeight;            // Объемный вес
    private String deliveryDate;                // Дата доставки
    private String serviceType;                 // Тип сервиса
    private String payerType;                   // Тип плательщика
    private String error;                       // Ошибка при расчете
}
