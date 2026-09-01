package com.hubert.apartmentbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApartmentbookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApartmentbookingApplication.class, args);
    }

}