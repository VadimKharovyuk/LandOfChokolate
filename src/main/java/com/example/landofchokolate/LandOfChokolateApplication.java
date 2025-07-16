package com.example.landofchokolate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class LandOfChokolateApplication {

    public static void main(String[] args) {
        SpringApplication.run(LandOfChokolateApplication.class, args);
    }
///как удалить каегории в котрых есть продукты  ( каскад или деактироватьвать)
}
