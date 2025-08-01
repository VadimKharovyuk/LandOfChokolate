package com.example.landofchokolate.dto.novaposhta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovaPoshtaResponse<T> {
    private boolean success;
    private List<T> data;
    private List<String> errors;
    private List<String> warnings;
    private List<String> info;
    private String messageCodes;
    private String errorCodes;
    private String warningCodes;
    private String infoCodes;
}