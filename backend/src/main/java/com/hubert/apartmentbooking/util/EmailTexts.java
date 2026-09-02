package com.hubert.apartmentbooking.util;

public final class EmailTexts {

    private EmailTexts() {
    }

    public static final String RESET_PASSWORD_SUBJECT = "Reset hasła - Residenza Aurea";
    public static final String RESET_PASSWORD_BUTTON_TEXT = "Zresetuj hasło";
    public static final String RESET_PASSWORD_GREETING = "Cześć %s,";
    public static final String RESET_PASSWORD_INTRO =
            "Otrzymaliśmy prośbę o zresetowanie hasła do Twojego konta. "
                    + "Kliknij przycisk poniżej, aby ustawić nowe hasło. Link jest ważny przez ograniczony czas.";
    public static final String RESET_PASSWORD_FOOTER =
            "Jeśli to nie Ty prosiłeś o reset hasła, po prostu zignoruj tę wiadomość — Twoje hasło pozostanie bez zmian.";
    public static final String RESERVATION_CONFIRMATION_SUBJECT = "Potwierdzenie rezerwacji - Residenza Aurea";
    public static final String RESERVATION_CONFIRMATION_BUTTON_TEXT = "Zobacz szczegóły rezerwacji";
    public static final String RESERVATION_CONFIRMATION_GREETING = "Cześć %s,";
    public static final String RESERVATION_CONFIRMATION_INTRO =
            "Twoja rezerwacja została przyjęta. Termin: %s – %s, liczba gości: %d, łączna kwota: %s zł. "
                    + "Kliknij przycisk poniżej, aby zobaczyć pełne szczegóły rezerwacji.";
    public static final String RESERVATION_CONFIRMATION_FOOTER =
            "Jeśli nie dokonywałeś tej rezerwacji, skontaktuj się z nami odpowiadając na tę wiadomość.";
    public static final String RESERVATION_RECEIVED_SUBJECT = "Twoja rezerwacja czeka na płatność - Residenza Aurea";
    public static final String RESERVATION_RECEIVED_BUTTON_TEXT = "Zobacz status rezerwacji";
    public static final String RESERVATION_RECEIVED_GREETING = "Cześć %s,";
    public static final String RESERVATION_RECEIVED_INTRO =
            "Otrzymaliśmy Twoją prośbę o rezerwację. Termin: %s – %s, liczba gości: %d, łączna kwota: %s zł. "
                    + "Dokończ płatność w ciągu %d minut, aby potwierdzić rezerwację — po tym czasie termin zostanie zwolniony.";
    public static final String RESERVATION_RECEIVED_FOOTER =
            "Jeśli nie dokonywałeś tej rezerwacji, zignoruj tę wiadomość — rezerwacja wygaśnie automatycznie.";
    public static final String RESERVATION_CONFIRMATION_ACCESS_INTRO =
            "Poniżej znajdziesz informacje, jak dostać się do mieszkania:";
    public static final String RESERVATION_CONFIRMATION_ACCESS_DETAILS =
            "Gdy podjedziesz pod wskazany adres, otwórz klatkę kodem %s, a następnie podejdź do drzwi mieszkania "
                    + "i otwórz skrytkę z kluczami kodem %s. Po zakończonej rezerwacji prosimy o wrzucenie kluczy "
                    + "do skrzynki na listy z numerem %s (numer mieszkania).";
}