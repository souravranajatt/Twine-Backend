package com.loginapp.loginapp.aspect;

import java.lang.reflect.Method;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.annotation.RateLimit;
import com.loginapp.loginapp.service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Aspect
@Component
public class RateLimitAspect {

    private final RateLimitService rateLimitService;
    private final AuthUtils authUtils;

    public RateLimitAspect(RateLimitService rateLimitService, AuthUtils authUtils) {
        this.rateLimitService = rateLimitService;
        this.authUtils = authUtils;
    }

    // Intercept methods marked with @RateLimit
    @Around("@annotation(rateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        HttpServletResponse response = attributes != null ? attributes.getResponse() : null;

        // Use logged-in userId if available, otherwise fallback to client IP
        String clientIdentifier = resolveClientIdentifier(request);

        // Figure out action key name
        String actionKey = rateLimit.key();
        if (actionKey == null || actionKey.isBlank()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            actionKey = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }

        // Ask Redis if this request is within limit
        RateLimitService.RateLimitResult result = rateLimitService.checkLimit(
            actionKey,
            clientIdentifier,
            rateLimit.maxRequests(),
            rateLimit.windowSeconds()
        );

        // Limit exceeded -> Send HTTP 429 with retry info
        if (!result.allowed()) {
            if (response != null) {
                response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
                response.setHeader("X-RateLimit-Limit", String.valueOf(result.maxRequests()));
                response.setHeader("X-RateLimit-Remaining", "0");
            }
            Map<String, Object> errorBody = Map.of(
                "status", 429,
                "error", "Too Many Requests",
                "message", rateLimit.message(),
                "retryAfterSeconds", result.retryAfterSeconds()
            );
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorBody);
        }

        // Add rate limit headers to response
        if (response != null) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(result.maxRequests()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, result.maxRequests() - result.currentRequests())));
        }

        return joinPoint.proceed();
    }

    // Helper to get user ID or proxy client IP
    private String resolveClientIdentifier(HttpServletRequest request) {
        Long userId = authUtils.getLoggedUserIdOptional();
        if (userId != null) {
            return "user_" + userId;
        }

        if (request != null) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                return "ip_" + xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp)) {
                return "ip_" + xRealIp.trim();
            }

            return "ip_" + request.getRemoteAddr();
        }

        return "anonymous_unknown";
    }
}
