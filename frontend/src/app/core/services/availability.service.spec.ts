import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AvailabilityService } from './availability.service';
import { API_ENDPOINTS } from '../constants/constants';
import { AvailabilityResponse } from '../models/availability.model';

describe('AvailabilityService', () => {
  let service: AvailabilityService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AvailabilityService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should start with no unavailable dates', () => {
    expect(service.isDateUnavailable('2026-09-10')).toBeFalse();
  });

  it('loadAvailability() should GET with apartmentId/from/to query params and store the result as a Set', () => {
    const response: AvailabilityResponse = { unavailableDates: ['2026-09-10', '2026-09-11'] };

    service.loadAvailability(1, '2026-09-01', '2026-12-01').subscribe(res => {
      expect(res).toEqual(response);
    });

    const req = httpMock.expectOne(r => r.url === API_ENDPOINTS.availability);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('apartmentId')).toBe('1');
    expect(req.request.params.get('from')).toBe('2026-09-01');
    expect(req.request.params.get('to')).toBe('2026-12-01');
    req.flush(response);

    expect(service.isDateUnavailable('2026-09-10')).toBeTrue();
    expect(service.isDateUnavailable('2026-09-11')).toBeTrue();
    expect(service.isDateUnavailable('2026-09-12')).toBeFalse();
  });

  it('should replace the previous unavailable dates on a subsequent call', () => {
    service.loadAvailability(1, '2026-09-01', '2026-12-01').subscribe();
    httpMock.expectOne(r => r.url === API_ENDPOINTS.availability).flush({ unavailableDates: ['2026-09-10'] });
    expect(service.isDateUnavailable('2026-09-10')).toBeTrue();

    service.loadAvailability(1, '2027-01-01', '2027-03-01').subscribe();
    httpMock.expectOne(r => r.url === API_ENDPOINTS.availability).flush({ unavailableDates: ['2027-01-05'] });

    expect(service.isDateUnavailable('2026-09-10')).toBeFalse();
    expect(service.isDateUnavailable('2027-01-05')).toBeTrue();
  });
});
