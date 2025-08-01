package com.example.landofchokolate.dto.novaposhta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovaPoshtaRequest {
    private String apiKey;
    private String modelName;       // Address, Common, InternetDocument, TrackingDocument
    private String calledMethod;    // getCities, getWarehouses, save, getStatusDocuments
    private Object methodProperties; // Параметры метода
}