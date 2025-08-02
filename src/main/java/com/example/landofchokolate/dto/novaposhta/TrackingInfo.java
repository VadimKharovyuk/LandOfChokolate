package com.example.landofchokolate.dto.novaposhta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackingInfo {

    private String number;
    private String statusCode;
    private String dateCreated;
    private String status;
    private String citySender;
    private String cityRecipient;
    private String senderAddress;
    private String recipientAddress;
    private String recipientFullName;
    private String documentCost;
    private String announcedPrice;
    private String documentWeight;
    private String seatsAmount;
    private String serviceType;
    private String phoneSender;
    private String phoneRecipient;
    private String scheduledDeliveryDate;
    private String actualDeliveryDate;
    private String paymentMethod;
    private String payerType;
    private String cargoDescriptionString;
    private String cargoType;
    private String dateScan;
    private String dateMoving;
    private String trackingUpdateDate;
    private String warehouseSender;
    private String warehouseRecipient;
    private String counterpartySenderDescription;
    private String counterpartyRecipientDescription;

    // Методы для получения форматированных данных
    public String getFormattedStatus() {
        if (status == null) return "Невідомо";
        return status;
    }

    public String getFormattedCost() {
        if (documentCost == null || documentCost.isEmpty() || "0".equals(documentCost)) {
            return "Безкоштовно";
        }
        try {
            double cost = Double.parseDouble(documentCost);
            return String.format("%.0f грн", cost);
        } catch (NumberFormatException e) {
            return documentCost + " грн";
        }
    }

    public String getStatusBadgeClass() {
        if (statusCode == null) return "new";
        return switch (statusCode) {
            case "1" -> "new";           // Создана
            case "2" -> "paid";          // В обработке
            case "3" -> "ready";         // В пути
            case "4", "5" -> "completed"; // Доставлена
            case "6", "7" -> "cancelled"; // Отменена/возврат
            default -> "new";
        };
    }

    public boolean isDelivered() {
        return "4".equals(statusCode) || "5".equals(statusCode);
    }

    public boolean isInTransit() {
        return "2".equals(statusCode) || "3".equals(statusCode);
    }

    public boolean isCreated() {
        return "1".equals(statusCode);
    }

    public boolean isCancelled() {
        return "6".equals(statusCode) || "7".equals(statusCode);
    }

    // Получить адрес отправителя (приоритет полному адресу)
    public String getFullSenderAddress() {
        if (senderAddress != null && !senderAddress.trim().isEmpty()) {
            return senderAddress;
        }
        if (warehouseSender != null && !warehouseSender.trim().isEmpty()) {
            return (citySender != null ? citySender + ", " : "") + warehouseSender;
        }
        return citySender;
    }

    // Получить адрес получателя (приоритет полному адресу)
    public String getFullRecipientAddress() {
        if (recipientAddress != null && !recipientAddress.trim().isEmpty()) {
            return recipientAddress;
        }
        if (warehouseRecipient != null && !warehouseRecipient.trim().isEmpty()) {
            return (cityRecipient != null ? cityRecipient + ", " : "") + warehouseRecipient;
        }
        return cityRecipient;
    }

    // Получить описание содержимого
    public String getCargoDescription() {
        if (cargoDescriptionString != null && !cargoDescriptionString.trim().isEmpty()) {
            return cargoDescriptionString;
        }
        return cargoType != null ? cargoType : "Посилка";
    }

    // Получить дату последнего обновления
    public String getLastUpdateDate() {
        if (trackingUpdateDate != null && !trackingUpdateDate.trim().isEmpty()) {
            return trackingUpdateDate;
        }
        if (dateScan != null && !dateScan.trim().isEmpty()) {
            return dateScan;
        }
        return dateCreated;
    }
}