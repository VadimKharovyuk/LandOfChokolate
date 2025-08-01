package com.example.landofchokolate.dto.novaposhta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {
    @NotBlank
    private String citySender;

    @NotBlank
    private String cityRecipient;

    @NotBlank
    private String serviceType;

    @Positive
    private double weight;

    @Positive
    private double cost;
}
