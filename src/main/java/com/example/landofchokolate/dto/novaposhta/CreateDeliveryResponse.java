// Убедитесь что ваш CreateDeliveryResponse содержит все нужные поля

package com.example.landofchokolate.dto.novaposhta;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateDeliveryResponse {

    @JsonProperty("Ref")
    private String ref;

    @JsonProperty("IntDocNumber")
    private String intDocNumber;

    @JsonProperty("DocumentNumber")
    private String documentNumber;

    @JsonProperty("Number")
    private String number;

    @JsonProperty("CostOnSite")
    private String costOnSite;

    @JsonProperty("EstimatedDeliveryDate")
    private String estimatedDeliveryDate;

    @JsonProperty("TypeDocument")
    private String typeDocument;

    @JsonProperty("Error")
    private String error;

    // ✅ Дополнительные поля которые могут присутствовать в ответе
    @JsonProperty("BarCode")
    private String barCode;

    @JsonProperty("DocNumber")
    private String docNumber;

    @JsonProperty("DocumentWeight")
    private String documentWeight;

    @JsonProperty("CheckWeight")
    private String checkWeight;

    @JsonProperty("DocumentCost")
    private String documentCost;

    @JsonProperty("SenderFullNameEW")
    private String senderFullNameEW;

    @JsonProperty("RecipientFullNameEW")
    private String recipientFullNameEW;

    @JsonProperty("PhoneSender")
    private String phoneSender;

    @JsonProperty("PhoneRecipient")
    private String phoneRecipient;
}