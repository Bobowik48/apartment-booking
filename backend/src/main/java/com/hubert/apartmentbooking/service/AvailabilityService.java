package com.hubert.apartmentbooking.service;

import com.hubert.apartmentbooking.model.BlockedDate;
import com.hubert.apartmentbooking.model.Reservation;
import com.hubert.apartmentbooking.repository.BlockedDateRepository;
import com.hubert.apartmentbooking.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

@Service
public class AvailabilityService {

    private final ReservationRepository reservationRepository;
    private final BlockedDateRepository blockedDateRepository;

    public AvailabilityService(ReservationRepository reservationRepository, BlockedDateRepository blockedDateRepository) {
        this.reservationRepository = reservationRepository;
        this.blockedDateRepository = blockedDateRepository;
    }

    public List<LocalDate> getUnavailableDates(Long apartmentId, LocalDate from, LocalDate to) {
        List<Reservation> reservations = reservationRepository.findOverlapping(apartmentId, from, to);


        List<BlockedDate> blockedDates = blockedDateRepository.findOverlapping(apartmentId, from, to);

        TreeSet<LocalDate> unavailableDates = new TreeSet<>();

        for (Reservation reservation : reservations) {
            LocalDate date = reservation.getCheckInDate();
            while (date.isBefore(reservation.getCheckOutDate())) {
                if (!date.isBefore(from) && date.isBefore(to)) {
                    unavailableDates.add(date);
                }
                date = date.plusDays(1);
            }
        }

        for (BlockedDate blockedDate : blockedDates) {
            LocalDate date = blockedDate.getStartDate();
            while (!date.isAfter(blockedDate.getEndDate())) {
                if (!date.isBefore(from) && date.isBefore(to)) {
                    unavailableDates.add(date);
                }
                date = date.plusDays(1);
            }
        }

        return List.copyOf(unavailableDates);
    }
}