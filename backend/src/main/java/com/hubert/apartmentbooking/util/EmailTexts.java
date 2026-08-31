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
}