package com.example.landofchokolate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableScheduling
public class LandOfChokolateApplication {

    public static void main(String[] args) {
        SpringApplication.run(LandOfChokolateApplication.class, args);
    }
///баг кнопки доавление в корзину
}
