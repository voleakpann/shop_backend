package com.ministore.auth.security;

import com.ministore.auth.user.User;
import com.ministore.auth.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Runs after Google verifies the user. Stores/updates the user, issues our own
 * JWT, then redirects back to the frontend with the token in the query string.
 */
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository users;
    private final JwtService jwtService;
    private final String frontendRedirectUri;

    public OAuth2SuccessHandler(
            UserRepository users,
            JwtService jwtService,
            @Value("${ministore.frontend-redirect-uri:http://localhost:3000/account}") String frontendRedirectUri) {
        this.users = users;
        this.jwtService = jwtService;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");
        String providerId = oauthUser.getAttribute("sub");

        // Upsert the user record.
        User user = users.findByEmail(email).orElseGet(() -> new User(email, name, picture, providerId));
        user.setName(name);
        user.setPicture(picture);
        user.setProviderId(providerId);
        users.save(user);

        // Issue our JWT. The role is read from the DB record (never from the client),
        // so promoting a user to ADMIN in the DB takes effect on their next login.
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", name);
        claims.put("picture", picture);
        claims.put("role", user.getRole() == null ? "USER" : user.getRole());
        String token = jwtService.generateToken(email, claims);

        // Send the browser back to the frontend with the token.
        String target = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("token", token)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
