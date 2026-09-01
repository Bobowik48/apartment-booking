package com.hubert.apartmentbooking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class BackendUrlHolder {

    private final AtomicReference<String> url;

    public BackendUrlHolder(@Value("${app.backend.url}") String initialUrl) {
        this.url = new AtomicReference<>(initialUrl);
    }

    public String get() {
        return url.get();
    }

    public void set(String newUrl) {
        url.set(newUrl);
    }
}