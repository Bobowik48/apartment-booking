package com.hubert.apartmentbooking.config;

import com.hubert.apartmentbooking.constants.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(Constants.AVAILABILITY_PATH, Constants.RESERVATIONS_PATH, Constants.RESERVATIONS_PATH + "/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}