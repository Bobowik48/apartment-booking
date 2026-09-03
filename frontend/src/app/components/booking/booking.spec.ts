import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { Booking } from './booking';
import { AvailabilityService } from '../../core/services/availability.service';
import { ApartmentService } from '../../core/services/apartment.service';
import { ReservationService } from '../../core/services/reservation.service';
import { ErrorTranslationService } from '../../core/services/error-translation.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { Apartment } from '../../core/models/apartment.model';
import { ReservationResponse } from '../../core/models/reservation.model';

describe('Booking', () => {
  let component: Booking;
  let fixture: ComponentFixture<Booking>;

  let availabilityServiceSpy: jasmine.SpyObj<AvailabilityService>;
  let apartmentServiceSpy: jasmine.SpyObj<ApartmentService>;
  let reservationServiceSpy: jasmine.SpyObj<ReservationService>;
  let errorTranslationServiceSpy: jasmine.SpyObj<ErrorTranslationService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const sampleApartment: Apartment = {
    id: 1,
    name: 'Residenza Aurea',
    description: 'Piękny apartament',
    street: 'Kwiatowa',
    apartmentNumber: '4',
    district: 'Centrum',
    city: 'Warszawa',
    pricePerNight: 200,
    maxGuests: 4,
    area: 45,
    floor: 2,
    buildingEntranceCode: null,
    keyBoxCode: null
  };

  const sampleReservationResponse: ReservationResponse = {
    id: 1,
    checkInDate: '2026-09-10',
    checkOutDate: '2026-09-12',
    guestsCount: 2,
    totalPrice: 400,
    status: 'PENDING_PAYMENT',
    accessToken: 'token-123',
    guestName: 'Jan Kowalski',
    guestEmail: 'jan@example.com',
    guestPhone: '+48600000000',
    apartmentName: 'Residenza Aurea',
    apartmentStreet: 'Kwiatowa',
    apartmentNumber: '4',
    apartmentCity: 'Warszawa'
  };

  beforeEach(async () => {
    availabilityServiceSpy = jasmine.createSpyObj('AvailabilityService', ['loadAvailability', 'isDateUnavailable']);
    apartmentServiceSpy = jasmine.createSpyObj('ApartmentService', ['getApartment']);
    reservationServiceSpy = jasmine.createSpyObj('ReservationService', ['createReservation']);
    errorTranslationServiceSpy = jasmine.createSpyObj('ErrorTranslationService', ['translate']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'getMyProfile']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['success', 'error']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    availabilityServiceSpy.loadAvailability.and.returnValue(of({ unavailableDates: [] }));
    availabilityServiceSpy.isDateUnavailable.and.returnValue(false);
    apartmentServiceSpy.getApartment.and.returnValue(of(sampleApartment));
    authServiceSpy.isLoggedIn.and.returnValue(false);

    await TestBed.configureTestingModule({
      imports: [Booking],
      providers: [
        provideRouter([]),
        { provide: AvailabilityService, useValue: availabilityServiceSpy },
        { provide: ApartmentService, useValue: apartmentServiceSpy },
        { provide: ReservationService, useValue: reservationServiceSpy },
        { provide: ErrorTranslationService, useValue: errorTranslationServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Booking);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load the apartment on init', () => {
    expect(component).toBeTruthy();
    expect(component.apartment()).toEqual(sampleApartment);
    expect(apartmentServiceSpy.getApartment).toHaveBeenCalledWith(1);
    expect(availabilityServiceSpy.loadAvailability).toHaveBeenCalled();
  });

  it('should not fetch the user profile when the visitor is not logged in', () => {
    expect(authServiceSpy.getMyProfile).not.toHaveBeenCalled();
  });

  describe('nights / nightsLabel', () => {
    it('should be 0 with no dates selected', () => {
      expect(component.nights()).toBe(0);
    });

    it('should compute the correct number of nights and use singular form for 1 night', () => {
      component.selectedCheckIn.set(new Date(2026, 8, 10));
      component.selectedCheckOut.set(new Date(2026, 8, 11));
      expect(component.nights()).toBe(1);
      expect(component.nightsLabel()).toBe('noc');
    });

    it('should use "noce" for 2-4 nights (outside the teen exception)', () => {
      component.selectedCheckIn.set(new Date(2026, 8, 10));
      component.selectedCheckOut.set(new Date(2026, 8, 13));
      expect(component.nights()).toBe(3);
      expect(component.nightsLabel()).toBe('noce');
    });

    it('should use "nocy" for 5+ nights and for the 12-14 teen exception', () => {
      component.selectedCheckIn.set(new Date(2026, 8, 1));
      component.selectedCheckOut.set(new Date(2026, 8, 6));
      expect(component.nights()).toBe(5);
      expect(component.nightsLabel()).toBe('nocy');

      component.selectedCheckIn.set(new Date(2026, 8, 1));
      component.selectedCheckOut.set(new Date(2026, 8, 13));
      expect(component.nights()).toBe(12);
      expect(component.nightsLabel()).toBe('nocy');
    });
  });

  describe('guest field validation', () => {
    it('should require a name of at least 3 letters-only characters', () => {
      component.updateGuestName('');
      expect(component.nameError()).toContain('Podaj');
      component.updateGuestName('Al');
      expect(component.nameError()).toContain('co najmniej 3 znaki');
      component.updateGuestName('Al3x');
      expect(component.nameError()).toContain('tylko litery');
      component.updateGuestName('Jan Kowalski');
      expect(component.nameError()).toBeNull();
    });

    it('should validate the email format', () => {
      component.updateGuestEmail('');
      expect(component.emailError()).toContain('Podaj adres');
      component.updateGuestEmail('not-an-email');
      expect(component.emailError()).toContain('prawidłowy');
      component.updateGuestEmail('jan@example.com');
      expect(component.emailError()).toBeNull();
    });

    it('should validate the phone number has 9-15 digits', () => {
      component.updateGuestPhone('');
      expect(component.phoneError()).toContain('Podaj numer');
      component.updateGuestPhone('123');
      expect(component.phoneError()).toContain('9-15 cyfr');
      component.updateGuestPhone('+48 600 000 000');
      expect(component.phoneError()).toBeNull();
    });
  });

  describe('canSubmit / totalPrice', () => {
    function fillValidGuestData() {
      component.updateGuestName('Jan Kowalski');
      component.updateGuestEmail('jan@example.com');
      component.updateGuestPhone('+48600000000');
    }

    it('should be false when no nights are selected even if guest data is valid', () => {
      fillValidGuestData();
      expect(component.canSubmit()).toBeFalse();
    });

    it('should be true once dates and valid guest data are provided', () => {
      fillValidGuestData();
      component.selectedCheckIn.set(new Date(2026, 8, 10));
      component.selectedCheckOut.set(new Date(2026, 8, 12));
      expect(component.canSubmit()).toBeTrue();
    });

    it('should compute totalPrice from nights * pricePerNight', () => {
      component.selectedCheckIn.set(new Date(2026, 8, 10));
      component.selectedCheckOut.set(new Date(2026, 8, 12));
      expect(component.totalPrice()).toBe(2 * sampleApartment.pricePerNight);
    });
  });

  describe('guest count', () => {
    it('should increment up to the apartment max guests', () => {
      component.guestsCount.set(sampleApartment.maxGuests - 1);
      component.incrementGuests();
      expect(component.guestsCount()).toBe(sampleApartment.maxGuests);
      component.incrementGuests();
      expect(component.guestsCount()).toBe(sampleApartment.maxGuests);
    });

    it('should decrement but never below 1', () => {
      component.guestsCount.set(1);
      component.decrementGuests();
      expect(component.guestsCount()).toBe(1);
      component.guestsCount.set(2);
      component.decrementGuests();
      expect(component.guestsCount()).toBe(1);
    });
  });

  describe('onDateSelected', () => {
    it('should set the check-in date on the first click', () => {
      component.onDateSelected(new Date(2026, 8, 10));
      expect(component.selectedCheckIn()).toEqual(new Date(2026, 8, 10));
      expect(component.selectedCheckOut()).toBeNull();
    });

    it('should set the check-out date on the second click when after check-in', () => {
      component.onDateSelected(new Date(2026, 8, 10));
      component.onDateSelected(new Date(2026, 8, 12));
      expect(component.selectedCheckIn()).toEqual(new Date(2026, 8, 10));
      expect(component.selectedCheckOut()).toEqual(new Date(2026, 8, 12));
    });

    it('should restart the selection when clicking a date before or equal to check-in', () => {
      component.onDateSelected(new Date(2026, 8, 10));
      component.onDateSelected(new Date(2026, 8, 5));
      expect(component.selectedCheckIn()).toEqual(new Date(2026, 8, 5));
      expect(component.selectedCheckOut()).toBeNull();
    });

    it('should restart the selection if the range contains an unavailable date', () => {
      availabilityServiceSpy.isDateUnavailable.and.callFake((date: string) => date === '2026-09-11');

      component.onDateSelected(new Date(2026, 8, 10));
      component.onDateSelected(new Date(2026, 8, 12));

      expect(component.selectedCheckIn()).toEqual(new Date(2026, 8, 12));
      expect(component.selectedCheckOut()).toBeNull();
    });
  });

  describe('submitReservation', () => {
    beforeEach(() => {
      component.updateGuestName('Jan Kowalski');
      component.updateGuestEmail('jan@example.com');
      component.updateGuestPhone('+48600000000');
      component.selectedCheckIn.set(new Date(2026, 8, 10));
      component.selectedCheckOut.set(new Date(2026, 8, 12));
    });

    it('should do nothing and mark fields touched when the form is invalid', () => {
      component.updateGuestEmail('');
      component.submitReservation();

      expect(component.touchedEmail()).toBeTrue();
      expect(reservationServiceSpy.createReservation).not.toHaveBeenCalled();
    });

    it('should create the reservation and navigate to reservation-details on success', () => {
      reservationServiceSpy.createReservation.and.returnValue(of(sampleReservationResponse));

      component.submitReservation();

      expect(reservationServiceSpy.createReservation).toHaveBeenCalledWith(jasmine.objectContaining({
        apartmentId: 1,
        checkInDate: '2026-09-10',
        checkOutDate: '2026-09-12',
        guestName: 'Jan Kowalski',
        guestEmail: 'jan@example.com',
        guestPhone: '+48600000000'
      }));
      expect(notificationServiceSpy.success).toHaveBeenCalled();
      expect(routerSpy.navigate).toHaveBeenCalledWith(
        ['/reservation-details'],
        { queryParams: { token: sampleReservationResponse.accessToken } }
      );
    });

    it('should show a translated error message and stop submitting on failure', () => {
      errorTranslationServiceSpy.translate.and.returnValue('Wybrany termin nie jest już dostępny. Wybierz inny.');
      reservationServiceSpy.createReservation.and.returnValue(
        throwError(() => ({ error: { errorCode: 'DATES_NOT_AVAILABLE' } }))
      );

      component.submitReservation();

      expect(component.isSubmitting()).toBeFalse();
      expect(component.errorMessage()).toBe('Wybrany termin nie jest już dostępny. Wybierz inny.');
      // Availability is called once on init and once more after a DATES_NOT_AVAILABLE error
      expect(availabilityServiceSpy.loadAvailability).toHaveBeenCalledTimes(2);
    });
  });
});
