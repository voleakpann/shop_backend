package com.ministore.auth.config;

import com.ministore.auth.security.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final OAuth2SuccessHandler successHandler;

    public SecurityConfig(OAuth2SuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // health/info and the login entrypoints are public
                        .requestMatchers("/", "/error", "/actuator/**").permitAll()
                        // Swagger UI / OpenAPI docs are public
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                // The OAuth2 login flow needs a session to hold the "state" param.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .oauth2Login(oauth -> oauth.successHandler(successHandler));
        return http.build();
    }
}
