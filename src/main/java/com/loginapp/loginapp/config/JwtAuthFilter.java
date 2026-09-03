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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

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

                // Check if session is still active in Redis
                if (!authRedisService.isValidSession(userId, sessionId)) {
                    filterChain.doFilter(request, response);
                    return;
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

