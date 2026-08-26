import { Component, inject, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReservationService } from '../../core/services/reservation.service';
import { ReservationResponse, ReservationStatus } from '../../core/models/reservation.model';
import { RESERVATION_STATUS_INFO, StatusInfo, UI_TEXT } from '../../core/constants/constants';

@Component({
  selector: 'app-my-reservations',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './my-reservations.html',
  styleUrl: './my-reservations.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MyReservations implements OnInit {
  // ### Constants ###
  readonly text = UI_TEXT.myReservations;

  // ### Services ###
  private reservationService = inject(ReservationService);

  // ### Fields ###
  readonly reservations = signal<ReservationResponse[]>([]);
  readonly isLoading = signal(true);

  ngOnInit(): void {
    this.reservationService.getMyReservations().subscribe({
      next: reservations => {
        this.reservations.set(reservations);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  statusInfo(status: ReservationStatus): StatusInfo {
    return RESERVATION_STATUS_INFO[status];
  }

  nights(reservation: ReservationResponse): number {
    const checkIn = new Date(reservation.checkInDate).getTime();
    const checkOut = new Date(reservation.checkOutDate).getTime();
    return Math.round((checkOut - checkIn) / (1000 * 60 * 60 * 24));
  }
}