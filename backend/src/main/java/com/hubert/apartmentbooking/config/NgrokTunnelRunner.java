package com.hubert.apartmentbooking.config;

import com.ngrok.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class NgrokTunnelRunner {

    private static final Logger log = LoggerFactory.getLogger(NgrokTunnelRunner.class);

    private final BackendUrlHolder backendUrlHolder;
    private final String authtoken;
    private final int serverPort;

    public NgrokTunnelRunner(BackendUrlHolder backendUrlHolder,
                             @Value("${NGROK_AUTHTOKEN:}") String authtoken,
                             @Value("${server.port:8080}") int serverPort) {
        this.backendUrlHolder = backendUrlHolder;
        this.authtoken = authtoken;
        this.serverPort = serverPort;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startTunnel() {
        if (authtoken == null || authtoken.isBlank()) {
            log.info("NGROK_AUTHTOKEN nie ustawiony — pomijam tunel, używam app.backend.url tak jak jest.");
            return;
        }

        try {
            var session = Session.withAuthtokenFromEnv().connect();
            var forwarder = session.httpEndpoint().forward(new URL("http://localhost:" + serverPort));

            String publicUrl = forwarder.getUrl().toString();
            backendUrlHolder.set(publicUrl);
            log.info("Tunel ngrok aktywny — webhook PayU osiągalny pod: {}", publicUrl);
        } catch (Exception e) {
            log.error("Nie udało się odpalić tunelu ngrok — webhooki PayU nie będą lokalnie osiągalne", e);
        }
    }
}