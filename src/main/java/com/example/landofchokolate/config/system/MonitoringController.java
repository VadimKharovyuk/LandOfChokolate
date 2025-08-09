package com.example.landofchokolate.config.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    @Autowired
    private DatabaseMonitoringService databaseService;

    @GetMapping("/database")
    public DatabaseMonitoringService.DatabaseInfo getDatabaseStatus() {
        return databaseService.getDatabaseInfo();
    }
}