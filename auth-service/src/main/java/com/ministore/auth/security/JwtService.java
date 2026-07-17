package com.ministore.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/** Issues signed JWTs (HS256). The same secret is shared with product-service. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(
            @Value("${ministore.jwt.secret}") String secret,
            @Value("${ministore.jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationSeconds, ChronoUnit.SECONDS)))
                .signWith(key)
                .compact();
    }
}
