package com.example.landofchokolate;


import com.example.landofchokolate.config.DotenvApplicationContextInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class LandOfChokolateApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LandOfChokolateApplication.class);

        app.addInitializers(new DotenvApplicationContextInitializer());

        app.run(args);
    }
}