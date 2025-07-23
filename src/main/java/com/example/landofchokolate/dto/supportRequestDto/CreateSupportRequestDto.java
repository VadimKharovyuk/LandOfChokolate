package com.example.landofchokolate.dto.supportRequestDto;

import com.example.landofchokolate.enums.SupportTopic;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSupportRequestDto {


    @Email(message = "Email адреса повинна бути дійсною")
    private String email;

    @Length(min = 6, max = 13, message = "Номер телефону повинен містити від 6 до 13 символів")
    @NotBlank(message = "Будь ласка, вкажіть номер телефону")
    private String phoneNumber;

    @NotNull(message = "Тема звернення є обов'язковою")
    private SupportTopic topic;

    @NotBlank(message = "Повідомлення є обов'язковим")
    @Length(min = 6, max = 1000, message = "Повідомлення повинно містити від 6 до 1000 символів")
    private String message;
}