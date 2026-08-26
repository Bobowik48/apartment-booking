import { Component, inject, signal, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ReservationService } from '../../core/services/reservation.service';
import { ReservationResponse } from '../../core/models/reservation.model';
import { UI_TEXT } from '../../core/constants/constants';

@Component({
  selector: 'app-reservation-details',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './reservation-details.html',
  styleUrl: './reservation-details.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReservationDetails implements OnInit {
  // ### Constants ###
  readonly text = UI_TEXT.reservationDetails;

  // ### Dependencies ###
  private route = inject(ActivatedRoute);

  // ### Services ###
  private reservationService = inject(ReservationService);

  // ### Fields ###
  readonly reservation = signal<ReservationResponse | null>(null);
  readonly isLoading = signal(true);
  readonly notFound = signal(false);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.isLoading.set(false);
      this.notFound.set(true);
      return;
    }

    this.reservationService.getByAccessToken(token).subscribe({
      next: reservation => {
        this.reservation.set(reservation);
        this.isLoading.set(false);
      },
      error: () => {
        this.notFound.set(true);
        this.isLoading.set(false);
      }
    });
  }

  get accessLink(): string {
    const reservation = this.reservation();
    if (!reservation) return '';
    return `${window.location.origin}/reservation-details?token=${reservation.accessToken}`;
  }
}