package com.ministore.blog.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI setup. Declares a "bearerAuth" scheme so the Swagger UI shows an
 * "Authorize" button — paste an ADMIN JWT from auth-service there to call the
 * authoring endpoints. Reading posts needs no token.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Blog Service API", version = "1.0",
                description = "Blog posts and categories for MiniStore"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
