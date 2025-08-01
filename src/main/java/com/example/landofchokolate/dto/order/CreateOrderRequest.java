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
//    @NotBlank(message = "Email обязателен для заполнения")
    private String email;

    // Временно упрощенная валидация для отладки
//    @Pattern(regexp = "^(\\+38)?\\d{10}$", message = "Некорректный номер телефона")
//    @NotBlank(message = "Номер телефона обязателен")
    private String phoneNumber;

    @NotNull(message = "Способ доставки обязателен")
    private DeliveryMethod deliveryMethod;

    private String someNotes;

    // ✅ Новые поля для Nova Poshta
    private String recipientCityRef;    // Ref города получателя
    private String recipientAddressRef;

    // ID корзины или список товаров
    private Long cartId;
}
