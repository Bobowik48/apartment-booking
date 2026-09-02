package com.hubert.apartmentbooking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "apartments")
@Getter
@Setter
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String street;

    private String apartmentNumber;

    private String district;

    private String city;

    private BigDecimal pricePerNight;

    private Integer maxGuests;

    private BigDecimal area;

    private Integer floor;

    private String buildingEntranceCode;

    private String keyBoxCode;
}