import { Injectable } from '@angular/core';

// TODO: English and Ukraininan Language
const ERROR_TRANSLATIONS_PL: Record<string, string> = {
  APARTMENT_NOT_FOUND: 'Nie znaleziono apartamentu.',
  DATES_NOT_AVAILABLE: 'Wybrany termin nie jest już dostępny. Wybierz inny.',
  EMAIL_ALREADY_IN_USE: 'Ten adres email jest już zajęty.',
  INVALID_CREDENTIALS: 'Nieprawidłowy email lub hasło.',
  INVALID_EMAIL_FORMAT: 'Nieprawidłowy format adresu email.',
  WEAK_PASSWORD: 'Hasło jest zbyt słabe.',
  CHECK_OUT_BEFORE_CHECK_IN: 'Data wyjazdu musi być późniejsza niż data przyjazdu.',
  REQUIRED_FIELD_MISSING: 'Uzupełnij wszystkie wymagane pola.',
  GUESTS_COUNT_EXCEEDS_MAX: 'Przekroczono maksymalną liczbę gości.',
  USER_NOT_FOUND: 'Nie znaleziono użytkownika.',
  RESERVATION_NOT_FOUND: 'Nie znaleziono rezerwacji.',
  VALIDATION_FAILED: 'Wprowadzone dane są nieprawidłowe.',
  FILE_TOO_LARGE: 'Plik jest zbyt duży. Maksymalny rozmiar to 10 MB.',
};

const FALLBACK_MESSAGE_PL = 'Wystąpił nieoczekiwany błąd. Spróbuj ponownie.';

@Injectable({ providedIn: 'root' })
export class ErrorTranslationService {
  translate(errorCode: string | undefined | null): string {
    if (!errorCode) return FALLBACK_MESSAGE_PL;
    return ERROR_TRANSLATIONS_PL[errorCode] ?? FALLBACK_MESSAGE_PL;
  }
}