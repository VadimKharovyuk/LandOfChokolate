package com.example.landofchokolate.dto.novaposhta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryResponse {
    private String ref;                      // Уникальный ID накладной
    private BigDecimal costOnSite;           // Стоимость на сайте
    private String estimatedDeliveryDate;    // Расчетная дата доставки
    private String intDocNumber;             // Номер ТТН
    private String typeDocument;             // Тип документа
    private String error;                    // Ошибка создания
}
