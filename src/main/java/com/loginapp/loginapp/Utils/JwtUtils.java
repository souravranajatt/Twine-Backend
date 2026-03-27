package com.loginapp.loginapp.Utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.springframework.stereotype.Component;
import java.security.Key;

@Component
public class JwtUtils {

    // keep your secret here (in production move to env/secret manager)
    private final String jwtSecretKey = "x6f3u9knfr467fe46gf6ihfr3322zxvbm0899mx283ur8yc47ty3478jbv83hueu";
    // access token lifetime (ms). You can shorten in production.
    private final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7 Days

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
    }

    // Generate token with userId + username
    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    
}
