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
}