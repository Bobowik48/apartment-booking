import { TestBed } from '@angular/core/testing';

import { ErrorTranslationService } from './error-translation.service';

describe('ErrorTranslationService', () => {
  let service: ErrorTranslationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ErrorTranslationService);
  });

  it('should translate a known error code to its Polish message', () => {
    expect(service.translate('DATES_NOT_AVAILABLE'))
      .toBe('Wybrany termin nie jest już dostępny. Wybierz inny.');
    expect(service.translate('INVALID_CREDENTIALS'))
      .toBe('Nieprawidłowy email lub hasło.');
  });

  it('should fall back to the generic message for an unknown error code', () => {
    expect(service.translate('SOME_UNKNOWN_CODE')).toBe('Wystąpił nieoczekiwany błąd. Spróbuj ponownie.');
  });

  it('should fall back to the generic message for null or undefined', () => {
    expect(service.translate(null)).toBe('Wystąpił nieoczekiwany błąd. Spróbuj ponownie.');
    expect(service.translate(undefined)).toBe('Wystąpił nieoczekiwany błąd. Spróbuj ponownie.');
  });
});
