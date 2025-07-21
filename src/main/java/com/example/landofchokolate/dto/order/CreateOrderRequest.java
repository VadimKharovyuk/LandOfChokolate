package com.example.landofchokolate.dto.order;

import com.example.landofchokolate.enums.DeliveryMethod;
import lombok.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Имя обязательно для заполнения")
    private String customerName;

    @Email(message = "Некорректный email")
    @NotBlank(message = "Email обязателен для заполнения")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Некорректный номер телефона")
    @NotBlank(message = "Номер телефона обязателен")
    private String phoneNumber;

    @NotNull(message = "Способ доставки обязателен")
    private DeliveryMethod deliveryMethod;

    private String someNotes;

    // ID корзины или список товаров
    private Long cartId;
}
