package com.hubert.apartmentbooking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "apartment_photos")
@Getter
@Setter
public class ApartmentPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    private String fileName;

    private Integer displayOrder;

    private String altText;
}