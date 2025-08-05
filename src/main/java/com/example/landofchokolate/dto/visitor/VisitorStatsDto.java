package com.example.landofchokolate.dto.visitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

// 📝 DTO for API responses
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitorStatsDto {
    private Long totalVisits;
    private Long uniqueVisitors;
    private Long mobileVisits;
    private Long desktopVisits;
    private Map<String, Long> countriesStats;
    private Map<String, Long> citiesStats;
    private Map<String, Long> browsersStats;
    private List<PopularPageDto> popularPages;

    @Data
    @AllArgsConstructor
    public static class PopularPageDto {
        private String url;
        private Long visits;
        private Double percentage;
    }
}
