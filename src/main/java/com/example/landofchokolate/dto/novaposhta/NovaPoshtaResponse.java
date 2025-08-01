// ============================================================================
// dto/novaposhta/NovaPoshtaResponse.java - ИСПРАВЛЕННАЯ ВЕРСИЯ
// ============================================================================
package com.example.landofchokolate.dto.novaposhta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Игнорируем неизвестные поля
public class NovaPoshtaResponse<T> {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("data")
    private List<T> data;

    @JsonProperty("errors")
    private List<String> errors;

    @JsonProperty("warnings")
    private List<String> warnings;

    // ✅ ИСПРАВЛЕНО: info может быть объектом или массивом
    @JsonProperty("info")
    private Object info; // Используем Object вместо List<String>

    @JsonProperty("messageCodes")
    private List<String> messageCodes;

    @JsonProperty("errorCodes")
    private List<String> errorCodes;

    @JsonProperty("warningCodes")
    private List<String> warningCodes;

    @JsonProperty("infoCodes")
    private List<String> infoCodes;

    // ✅ Добавим удобный метод для получения totalCount из info
    public Integer getTotalCount() {
        if (info instanceof Map) {
            Map<?, ?> infoMap = (Map<?, ?>) info;
            Object totalCount = infoMap.get("totalCount");
            if (totalCount instanceof Number) {
                return ((Number) totalCount).intValue();
            }
        }
        return null;
    }
}