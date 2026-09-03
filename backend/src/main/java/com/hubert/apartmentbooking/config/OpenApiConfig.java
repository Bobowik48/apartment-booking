package com.hubert.apartmentbooking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI/Swagger documentation exposed by springdoc-openapi.
 * <p>
 * Once the application is running, the docs are available at:
 * <ul>
 *   <li>Swagger UI: {@code /swagger-ui.html} (or {@code /swagger-ui/index.html})</li>
 *   <li>Raw OpenAPI JSON: {@code /v3/api-docs}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI apartmentBookingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Apartment Booking API")
                        .description("REST API for browsing an apartment, checking availability, "
                                + "creating reservations, paying via PayU and managing user accounts.")
                        .version("v1")
                        .contact(new Contact().name("Apartment Booking")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by /api/auth/login or /api/auth/register "
                                        + "(without the \"Bearer \" prefix).")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME));
    }
}
