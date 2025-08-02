//package com.example.landofchokolate.dto.order;
//
//import com.example.landofchokolate.enums.DeliveryMethod;
//import lombok.*;
//import jakarta.validation.constraints.*;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class CreateOrderRequest {
//
//    @NotBlank(message = "Имя обязательно для заполнения")
//    private String customerName;
//
//    @Email(message = "Некорректный email")
////    @NotBlank(message = "Email обязателен для заполнения")
//    private String email;
//
//    // Временно упрощенная валидация для отладки
////    @Pattern(regexp = "^(\\+38)?\\d{10}$", message = "Некорректный номер телефона")
////    @NotBlank(message = "Номер телефона обязателен")
//    private String phoneNumber;
//
//    @NotNull(message = "Способ доставки обязателен")
//    private DeliveryMethod deliveryMethod;
//
//    private String someNotes;
//
//    // ✅ Новые поля для Nova Poshta
//    private String recipientCityRef;    // Ref города получателя
//    private String recipientAddressRef;
//
//    // ID корзины или список товаров
//    private Long cartId;
//}
package com.example.landofchokolate.dto.order;

import com.example.landofchokolate.enums.DeliveryMethod;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Имя обязательно для заполнения")
    private String customerName;

    @Email(message = "Некорректный email")
    private String email;

    // Временно упрощенная валидация для отладки
    private String phoneNumber;

    @NotNull(message = "Способ доставки обязателен")
    private DeliveryMethod deliveryMethod;

    private String someNotes;

    // ✅ Полные поля для Nova Poshta доставки
    private String recipientCityRef;        // Ref города получателя
    private String recipientCityName;       // Название города (для отображения)
    private String recipientAddressRef;     // Ref отделения
    private String recipientAddressName;    // Название отделения (для отображения)

    // Данные получателя (могут отличаться от заказчика)
    private String recipientFirstName;
    private String recipientLastName;
    private String recipientPhone;

    // ID корзины или список товаров
    private Long cartId;

    // ✅ Вспомогательные методы для валидации и получения данных

    /**
     * Проверяет валидность полей Nova Poshta
     */
    public boolean isNovaPoshtaFieldsValid() {
        if (deliveryMethod != DeliveryMethod.NOVA_POSHTA) {
            return true; // Для других методов доставки эти поля не обязательны
        }

        return recipientCityRef != null && !recipientCityRef.trim().isEmpty() &&
                recipientAddressRef != null && !recipientAddressRef.trim().isEmpty();
    }

    /**
     * Получить телефон получателя или заказчика
     */
    public String getEffectiveRecipientPhone() {
        return (recipientPhone != null && !recipientPhone.trim().isEmpty())
                ? recipientPhone
                : phoneNumber;
    }

    /**
     * Получить имя получателя или заказчика
     */
    public String getEffectiveRecipientName() {
        if (recipientFirstName != null && !recipientFirstName.trim().isEmpty()) {
            String fullName = recipientFirstName.trim();
            if (recipientLastName != null && !recipientLastName.trim().isEmpty()) {
                fullName += " " + recipientLastName.trim();
            }
            return fullName;
        }
        return customerName;
    }

    /**
     * Проверяет, заполнены ли персональные данные получателя
     */
    public boolean hasRecipientDetails() {
        return (recipientFirstName != null && !recipientFirstName.trim().isEmpty()) ||
                (recipientLastName != null && !recipientLastName.trim().isEmpty()) ||
                (recipientPhone != null && !recipientPhone.trim().isEmpty());
    }

    /**
     * Получить полное название места доставки для отображения
     */
    public String getDeliveryLocationDisplay() {
        if (deliveryMethod == DeliveryMethod.NOVA_POSHTA &&
                recipientCityName != null && recipientAddressName != null) {
            return recipientCityName + " - " + recipientAddressName;
        }
        return null;
    }
}