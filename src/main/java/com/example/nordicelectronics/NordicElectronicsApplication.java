package com.example.nordicelectronics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NordicElectronicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NordicElectronicsApplication.class, args);
    }

}
