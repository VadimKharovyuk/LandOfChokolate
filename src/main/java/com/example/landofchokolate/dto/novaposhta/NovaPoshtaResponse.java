package com.example.landofchokolate.dto.novaposhta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Игнорируем ВСЕ неизвестные поля
public class NovaPoshtaResponse<T> {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("data")
    private List<T> data;

    @JsonProperty("errors")
    private List<String> errors;



    @JsonProperty("messageCodes")
    private List<String> messageCodes;

    @JsonProperty("errorCodes")
    private List<String> errorCodes;

    // Простые методы проверки
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public String getFirstError() {
        return hasErrors() ? errors.get(0) : null;
    }

    public boolean hasData() {
        return data != null && !data.isEmpty();
    }
}