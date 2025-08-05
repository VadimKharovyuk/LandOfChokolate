package com.example.landofchokolate.dto.visitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationInfo {
    private String country;
    private String city;
    private String isp;
}