package com.loginapp.loginapp.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Annotation to put rate limiting on any controller method via Redis
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    // Action name (e.g. "AUTH_LOGIN", "POST_LIKE")
    String key() default "";

    // Max requests allowed in the window
    int maxRequests() default 10;

    // Time window in seconds (default: 60s)
    int windowSeconds() default 60;

    // Error message sent back when user hits the limit
    String message() default "Too many requests! Please slow down and try again shortly.";
}
