import { Component, inject, signal, computed, OnInit, ViewChild, ChangeDetectionStrategy } from '@angular/core';
import { DatePipe, UpperCasePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatCalendar, MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { AvailabilityService } from '../../core/services/availability.service';
import { ApartmentService } from '../../core/services/apartment.service';
import { Apartment } from '../../core/models/apartment.model';
import { DEFAULT_APARTMENT_ID, UI_TEXT } from '../../core/constants/constants';
import { ReservationService } from '../../core/services/reservation.service';
import { ErrorTranslationService } from '../../core/services/error-translation.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [MatDatepickerModule, MatNativeDateModule, DatePipe, UpperCasePipe, RouterLink],
  templateUrl: './booking.html',
  styleUrl: './booking.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Booking implements OnInit {
  // ### Constants ###
  readonly text = UI_TEXT.booking;

  // ### Dependencies ###
  private availabilityService = inject(AvailabilityService);
  private router = inject(Router);

  // ### Services ###
  private apartmentService = inject(ApartmentService);
  private reservationService = inject(ReservationService);
  private errorTranslationService = inject(ErrorTranslationService);
  private authService = inject(AuthService);

  // ### Constants ###
  readonly today = new Date();
  readonly maxDate = this.calculateMaxDate();
  private readonly namePattern = /^[\p{L}\s'-]+$/u;
  private readonly emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  private readonly phonePattern = /^\+?[0-9]{9,15}$/;

  // ### Fields ###
  readonly apartment = signal<Apartment | null>(null);
  readonly selectedCheckIn = signal<Date | null>(null);
  readonly selectedCheckOut = signal<Date | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly guestName = signal('');
  readonly guestEmail = signal('');
  readonly guestPhone = signal('');
  readonly isSubmitting = signal(false);
  readonly isAvailabilityLoaded = signal(false);
  readonly guestsCount = signal(1);
  readonly isLoggedIn = signal(this.authService.isLoggedIn());
  readonly touchedName = signal(false);
  readonly touchedEmail = signal(false);
  readonly touchedPhone = signal(false);
  @ViewChild(MatCalendar) private calendar?: MatCalendar<Date>;

  readonly nights = computed(() => {
    const checkIn = this.selectedCheckIn();
    const checkOut = this.selectedCheckOut();
    if (!checkIn || !checkOut) return 0;
    const diffMs = checkOut.getTime() - checkIn.getTime();
    return Math.round(diffMs / (1000 * 60 * 60 * 24));
  });

  readonly nightsLabel = computed(() => {
    const n = this.nights();
    if (n === 1) return 'noc';
    const lastDigit = n % 10;
    const lastTwoDigits = n % 100;
    const isTeenException = lastTwoDigits >= 12 && lastTwoDigits <= 14;
    if (lastDigit >= 2 && lastDigit <= 4 && !isTeenException) return 'noce';
    return 'nocy';
  });

  readonly nameError = computed(() => {
    const name = this.guestName().trim();
    if (name.length === 0) return 'Podaj imię i nazwisko.';
    if (name.length < 3) return 'Imię i nazwisko musi mieć co najmniej 3 znaki.';
    if (!this.namePattern.test(name)) return 'Imię i nazwisko może zawierać tylko litery.';
    return null;
  });

  readonly emailError = computed(() => {
    const email = this.guestEmail().trim();
    if (email.length === 0) return 'Podaj adres e-mail.';
    if (!this.emailPattern.test(email)) return 'Podaj prawidłowy adres e-mail.';
    return null;
  });

  readonly phoneError = computed(() => {
    const phone = this.guestPhone().trim();
    if (phone.length === 0) return 'Podaj numer telefonu.';
    const digitsOnly = phone.replace(/[\s-]/g, '');
    if (!this.phonePattern.test(digitsOnly)) return 'Podaj prawidłowy numer telefonu (9-15 cyfr).';
    return null;
  });

  readonly canSubmit = computed(() => {
    return this.nights() > 0
      && !this.nameError()
      && !this.emailError()
      && !this.phoneError()
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

    if (this.isLoggedIn()) {
      this.authService.getMyProfile().subscribe(profile => {
        this.guestName.set(profile.fullName);
        this.guestEmail.set(profile.email);
        this.guestPhone.set(profile.phone);
      });
    }
  }

  dateFilterFn = (date: Date | null): boolean => {
    if (!date) return false;
    return !this.availabilityService.isDateUnavailable(this.formatDate(date));
  };

  dateClassFn = (date: Date, view: string): string => {
    if (view !== 'month') return '';

    const checkIn = this.selectedCheckIn();
    const checkOut = this.selectedCheckOut();
    if (!checkIn) return '';

    const time = date.getTime();

    if (checkOut) {
      if (time === checkIn.getTime()) return 'range-start';
      if (time === checkOut.getTime()) return 'range-end';
      if (time > checkIn.getTime() && time < checkOut.getTime()) return 'range-middle';
    } else if (time === checkIn.getTime()) {
      return 'range-start';
    }

    return '';
  };

  onDateSelected(date: Date | null): void {
    if (!date) return;

    const checkIn = this.selectedCheckIn();

    if (!checkIn || this.selectedCheckOut()) {
      this.selectedCheckIn.set(date);
      this.selectedCheckOut.set(null);
    } else if (date <= checkIn) {
      this.selectedCheckIn.set(date);
    } else if (this.hasUnavailableDateInRange(checkIn, date)) {
      this.selectedCheckIn.set(date);
      this.selectedCheckOut.set(null);
    } else {
      this.selectedCheckOut.set(date);
    }

    this.calendar?.updateTodaysDate();
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

  markNameTouched(): void {
    this.touchedName.set(true);
  }

  markEmailTouched(): void {
    this.touchedEmail.set(true);
  }

  markPhoneTouched(): void {
    this.touchedPhone.set(true);
  }

  submitReservation(): void {
    this.touchedName.set(true);
    this.touchedEmail.set(true);
    this.touchedPhone.set(true);

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
        this.errorMessage.set(this.errorTranslationService.translate(err.error?.errorCode));

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

  private hasUnavailableDateInRange(checkIn: Date, checkOut: Date): boolean {
    const cursor = new Date(checkIn);
    while (cursor < checkOut) {
      if (this.availabilityService.isDateUnavailable(this.formatDate(cursor))) {
        return true;
      }
      cursor.setDate(cursor.getDate() + 1);
    }
    return false;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}