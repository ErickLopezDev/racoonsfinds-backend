package com.racoonsfinds.backend.security;

import io.jsonwebtoken.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.function.Function;
import java.security.Key;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final String jwtSecret;
    private final long jwtExpirationMs;
    private final Key key;

    public JwtUtil(@Value("${security.jwt.secret}") String jwtSecret,
                   @Value("${security.jwt.expiration}") long jwtExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.key = Keys.hmacShaKeyFor(this.jwtSecret.getBytes());
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return getExpirationDate(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getSubject(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public Date getExpirationDate(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    private <T> T getClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return resolver.apply(claims);
    }
}
