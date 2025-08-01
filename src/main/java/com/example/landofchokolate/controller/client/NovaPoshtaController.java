package com.example.landofchokolate.controller.client;

import com.example.landofchokolate.dto.novaposhta.City;
import com.example.landofchokolate.dto.novaposhta.Department;
import com.example.landofchokolate.dto.novaposhta.TrackingInfo;
import com.example.landofchokolate.service.PoshtaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/novaposhta")
@RequiredArgsConstructor
public class NovaPoshtaController {

    private final PoshtaService poshtaService;

    @GetMapping("/cities")
    public ResponseEntity<List<City>> getCities() {
        List<City> cities = poshtaService.getCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/departments/{cityRef}")
    public ResponseEntity<List<Department>> getDepartments(@PathVariable String cityRef) {
        List<Department> departments = poshtaService.getDepartments(cityRef);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<TrackingInfo> trackDelivery(@PathVariable String trackingNumber) {
        TrackingInfo trackingInfo = poshtaService.trackDelivery(trackingNumber);
        if (trackingInfo != null) {
            return ResponseEntity.ok(trackingInfo);
        }
        return ResponseEntity.notFound().build();
    }
}
