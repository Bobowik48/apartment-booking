import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ReservationService } from './reservation.service';
import { API_ENDPOINTS } from '../constants/constants';
import { CreateReservationRequest, ReservationResponse } from '../models/reservation.model';

describe('ReservationService', () => {
  let service: ReservationService;
  let httpMock: HttpTestingController;

  const sampleResponse: ReservationResponse = {
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

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ReservationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('createReservation() should POST the request to the reservations endpoint', () => {
    const request: CreateReservationRequest = {
      apartmentId: 1,
      checkInDate: '2026-09-10',
      checkOutDate: '2026-09-12',
      guestsCount: 2,
      guestName: 'Jan Kowalski',
      guestEmail: 'jan@example.com',
      guestPhone: '+48600000000'
    };

    service.createReservation(request).subscribe(res => expect(res).toEqual(sampleResponse));

    const req = httpMock.expectOne(API_ENDPOINTS.reservations);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(sampleResponse);
  });

  it('getByAccessToken() should GET the reservation by its access token', () => {
    service.getByAccessToken('token-123').subscribe(res => expect(res).toEqual(sampleResponse));

    const req = httpMock.expectOne(`${API_ENDPOINTS.reservations}/token-123`);
    expect(req.request.method).toBe('GET');
    req.flush(sampleResponse);
  });

  it('getMyReservations() should GET the current user reservations list', () => {
    service.getMyReservations().subscribe(res => expect(res).toEqual([sampleResponse]));

    const req = httpMock.expectOne(`${API_ENDPOINTS.reservations}/my`);
    expect(req.request.method).toBe('GET');
    req.flush([sampleResponse]);
  });
});
