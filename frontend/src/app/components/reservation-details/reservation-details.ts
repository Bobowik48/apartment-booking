import { Component, inject, signal, computed, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ReservationService } from '../../core/services/reservation.service';
import { ReservationResponse } from '../../core/models/reservation.model';
import { UI_TEXT } from '../../core/constants/constants';
import { PaymentService } from '../../core/services/payment.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-reservation-details',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './reservation-details.html',
  styleUrl: './reservation-details.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReservationDetails implements OnInit, OnDestroy {
  readonly text = UI_TEXT.reservationDetails;
  private readonly pollIntervalMs = 3000;
  private readonly maxPollAttempts = 20;

  private route = inject(ActivatedRoute);

  private reservationService = inject(ReservationService);
  private paymentService = inject(PaymentService);
  private notificationService = inject(NotificationService);

  readonly reservation = signal<ReservationResponse | null>(null);
  readonly isLoading = signal(true);
  readonly notFound = signal(false);
  readonly isInitiatingPayment = signal(false);
  readonly paymentError = signal<string | null>(null);
  private accessToken: string | null = null;
  private pollTimeoutId?: ReturnType<typeof setTimeout>;
  private pollAttempts = 0;

  readonly nights = computed(() => {
    const res = this.reservation();
    if (!res) return 0;
    const diffMs = new Date(res.checkOutDate).getTime() - new Date(res.checkInDate).getTime();
    return Math.round(diffMs / (1000 * 60 * 60 * 24));
  });

  readonly nightsWord = computed(() => {
    const n = this.nights();
    if (n === 1) return 'noc';
    const lastDigit = n % 10;
    const lastTwoDigits = n % 100;
    const isTeenException = lastTwoDigits >= 12 && lastTwoDigits <= 14;
    if (lastDigit >= 2 && lastDigit <= 4 && !isTeenException) return 'noce';
    return 'nocy';
  });

  readonly pricePerNight = computed(() => {
    const res = this.reservation();
    const nights = this.nights();
    if (!res || nights === 0) return 0;
    return Math.round((res.totalPrice / nights) * 100) / 100;
  });

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.isLoading.set(false);
      this.notFound.set(true);
      return;
    }

    this.accessToken = token;

    this.reservationService.getByAccessToken(token).subscribe({
      next: reservation => {
        this.reservation.set(reservation);
        this.isLoading.set(false);

        if (reservation.status === 'PENDING_PAYMENT') {
          this.schedulePoll();
        }
      },
      error: () => {
        this.notFound.set(true);
        this.isLoading.set(false);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.pollTimeoutId) {
      clearTimeout(this.pollTimeoutId);
    }
  }

  payNow(): void {
    const res = this.reservation();
    if (!res) return;

    this.isInitiatingPayment.set(true);
    this.paymentError.set(null);

    this.paymentService.initPayment(res.id).subscribe({
      next: response => {
        window.location.href = response.redirectUrl;
      },
      error: () => {
        this.isInitiatingPayment.set(false);
        this.paymentError.set(this.text.paymentError);
      }
    });
  }

  private schedulePoll(): void {
    if (this.pollAttempts >= this.maxPollAttempts || !this.accessToken) return;

    this.pollTimeoutId = setTimeout(() => {
      this.pollAttempts++;
      this.pollOnce();
    }, this.pollIntervalMs);
  }

  private pollOnce(): void {
    if (!this.accessToken) return;

    this.reservationService.getByAccessToken(this.accessToken).subscribe({
      next: reservation => {
        const previousStatus = this.reservation()?.status;
        this.reservation.set(reservation);

        if (previousStatus === 'PENDING_PAYMENT' && reservation.status === 'CONFIRMED') {
          this.notificationService.success('Płatność zaakceptowana! Rezerwacja potwierdzona, instrukcje dojazdu wysłaliśmy mailem.');
        }

        if (reservation.status === 'PENDING_PAYMENT') {
          this.schedulePoll();
        }
      },
      error: () => {
      }
    });
  }
}