import { Component, inject, signal, computed, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { AvailabilityService } from '../../core/services/availability.service';
import { ApartmentService } from '../../core/services/apartment.service';
import { Apartment } from '../../core/models/apartment.model';
import { DEFAULT_APARTMENT_ID } from '../../core/constants/constants';
import { ReservationService } from '../../core/services/reservation.service';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [MatDatepickerModule, MatNativeDateModule, DatePipe],
  templateUrl: './booking.html',
  styleUrl: './booking.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Booking implements OnInit {
  // ### Dependencies ###
  private availabilityService = inject(AvailabilityService);
  private router = inject(Router);

  // ### Services ###
  private apartmentService = inject(ApartmentService);
  private reservationService = inject(ReservationService);

  // ### Constants ###
  readonly today = new Date();
  readonly maxDate = this.calculateMaxDate();

  // ### Fields ###
  readonly apartment = signal<Apartment | null>(null);
  readonly selectedCheckIn = signal<Date | null>(null);
  readonly selectedCheckOut = signal<Date | null>(null);
  readonly guestName = signal('');
  readonly guestEmail = signal('');
  readonly guestPhone = signal('');
  readonly isSubmitting = signal(false);
  readonly isAvailabilityLoaded = signal(false);
  readonly guestsCount = signal(1);
  readonly nights = computed(() => {
    const checkIn = this.selectedCheckIn();
    const checkOut = this.selectedCheckOut();
    if (!checkIn || !checkOut) return 0;
    const diffMs = checkOut.getTime() - checkIn.getTime();
    return Math.round(diffMs / (1000 * 60 * 60 * 24));
  });
  readonly canSubmit = computed(() => {
    return this.nights() > 0
      && this.guestName().trim().length > 0
      && this.guestEmail().trim().length > 0
      && this.guestPhone().trim().length > 0
      && !this.isSubmitting();
  });

  readonly totalPrice = computed(() => {
    const apartment = this.apartment();
    return this.nights() * (apartment?.pricePerNight ?? 0);
  });

  ngOnInit(): void {
    this.apartmentService.getApartment(DEFAULT_APARTMENT_ID)
      .subscribe(apartment => this.apartment.set(apartment));

    const from = this.formatDate(this.today);
    const to = this.formatDate(this.maxDate);
    this.availabilityService.loadAvailability(DEFAULT_APARTMENT_ID, from, to)
      .subscribe(() => this.isAvailabilityLoaded.set(true));
  }

  dateFilterFn = (date: Date | null): boolean => {
    if (!date) return false;
    return !this.availabilityService.isDateUnavailable(this.formatDate(date));
  };

  onDateSelected(date: Date | null): void {
    if (!date) return;

    const checkIn = this.selectedCheckIn();

    if (!checkIn || this.selectedCheckOut()) {
      this.selectedCheckIn.set(date);
      this.selectedCheckOut.set(null);
      return;
    }

    if (date <= checkIn) {
      this.selectedCheckIn.set(date);
      return;
    }

    this.selectedCheckOut.set(date);
  }

  incrementGuests(): void {
    const max = this.apartment()?.maxGuests ?? 10;
    if (this.guestsCount() < max) this.guestsCount.update(v => v + 1);
  }

  decrementGuests(): void {
    if (this.guestsCount() > 1) this.guestsCount.update(v => v - 1);
  }

  updateGuestName(value: string): void {
    this.guestName.set(value);
  }

  updateGuestEmail(value: string): void {
    this.guestEmail.set(value);
  }

  updateGuestPhone(value: string): void {
    this.guestPhone.set(value);
  }

  submitReservation(): void {
    if (!this.canSubmit()) return;

    const checkIn = this.selectedCheckIn();
    const checkOut = this.selectedCheckOut();
    if (!checkIn || !checkOut) return;

    this.isSubmitting.set(true);

    this.reservationService.createReservation({
      apartmentId: DEFAULT_APARTMENT_ID,
      checkInDate: this.formatDate(checkIn),
      checkOutDate: this.formatDate(checkOut),
      guestsCount: this.guestsCount(),
      guestName: this.guestName(),
      guestEmail: this.guestEmail(),
      guestPhone: this.guestPhone()
    }).subscribe({
      next: response => {
        this.router.navigate(['/reservation-details'], {
          queryParams: { token: response.accessToken }
        });
      },
      error: (err) => {
        this.isSubmitting.set(false);
        if (err.error?.errorCode === 'DATES_NOT_AVAILABLE') {
          this.availabilityService.loadAvailability(
            DEFAULT_APARTMENT_ID,
            this.formatDate(this.today),
            this.formatDate(this.maxDate)
          ).subscribe();
        }
      }
    });
  }

  private calculateMaxDate(): Date {
    const max = new Date(this.today);
    max.setMonth(max.getMonth() + 3);
    return max;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}