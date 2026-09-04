package com.loginapp.loginapp.config;

import com.loginapp.loginapp.Utils.JwtUtils;
import com.loginapp.loginapp.service.AuthRedisService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${app.secure-cookie}")
    private boolean secureCookie;

    private final JwtUtils jwtUtils;
    private final AuthRedisService authRedisService;

    JwtAuthFilter(JwtUtils jwtUtils, AuthRedisService authRedisService) {
        this.jwtUtils = jwtUtils;
        this.authRedisService = authRedisService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        //  Read token from cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("token")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        try {
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                Claims claims = jwtUtils.extractAllClaims(token);
                String userId = claims.getSubject();
                String sessionId = claims.get("sessionId", String.class);
                String username = claims.get("username", String.class);

                // Check if session is still active in Redis
                if (!authRedisService.isValidSession(userId, sessionId)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Update Redis TTL If user active for next 7 days
                authRedisService.updateLastActive(sessionId);

                // If remaining token time less than 3 days, auto-refresh JWT cookie
                long remainingMs = claims.getExpiration().getTime() - System.currentTimeMillis();
                if (remainingMs < (3L * 24 * 60 * 60 * 1000L)) {
                    try {
                        Long userUid = Long.parseLong(userId);
                        String newToken = jwtUtils.generateToken(userUid, username, sessionId);

                        Cookie newCookie = new Cookie("token", newToken);
                        newCookie.setHttpOnly(true);
                        newCookie.setSecure(secureCookie);
                        newCookie.setPath("/");
                        newCookie.setMaxAge(7 * 24 * 60 * 60);
                        newCookie.setAttribute("SameSite", "Lax");
                        response.addCookie(newCookie);
                    } catch (Exception ex) {
                        System.out.println("Failed to refresh sliding session: " + ex.getMessage());
                    }
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, null);

                auth.setDetails(sessionId);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            System.out.println("JWT from cookie INVALID: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

