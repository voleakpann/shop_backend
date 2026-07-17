package com.ministore.auth.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** Returns the currently OAuth2-authenticated user (during an active login session). */
    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return Map.of();
        }
        return Map.of(
                "email", user.getAttribute("email"),
                "name", user.getAttribute("name"),
                "picture", user.getAttribute("picture"));
    }
}
