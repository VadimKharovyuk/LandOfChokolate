package com.example.landofchokolate.dto.novaposhta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingInfo {
    private String number;                   // Номер ТТН
    private String status;                   // Статус отправления
    private String statusCode;               // Код статуса
    private LocalDateTime dateCreated;       // Дата создания
    private LocalDateTime datePayedKeeping;  // Дата платного хранения
    private String recipientDateTime;        // Дата получения
    private String weight;                   // Фактический вес
    private String cost;                     // Стоимость доставки
    private String citySender;               // Город отправителя
    private String cityRecipient;            // Город получателя
    private String senderAddress;            // Адрес отправителя
    private String recipientAddress;         // Адрес получателя
    private String phoneRecipient;           // Телефон получателя
    private String recipientFullName;        // ФИО получателя
    private String warehouseRecipient;       // Отделение получателя
    private String cargoDescriptionString;   // Описание груза
    private String cargoType;                // Тип груза
    private String payerType;                // Тип плательщика
    private String paymentMethod;            // Способ оплаты
    private String packageCount;             // Количество мест
    private String actualDeliveryDate;       // Фактическая дата доставки
    private String lastCreatedOnTheBasisNumber; // Последний созданный документ
    private List<TrackingEvent> trackingEvents; // История статусов
    private String error;                    // Ошибка отслеживания

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingEvent {
        private String status;
        private LocalDateTime date;
        private String location;
        private String description;
    }
}

