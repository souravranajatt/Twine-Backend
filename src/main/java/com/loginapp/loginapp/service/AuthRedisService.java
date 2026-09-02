package com.loginapp.loginapp.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.loginapp.loginapp.Utils.SnowflakeIdGenerator;
import com.loginapp.loginapp.entity.UserSession;
import com.loginapp.loginapp.repository.UserSessionRepo;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthRedisService {

    private final UserSessionRepo userSessionRepo;

    AuthRedisService(UserSessionRepo userSessionRepo) {
        this.userSessionRepo = userSessionRepo;
    }

    public String parseDeviceName(String userAgent) {
        if (userAgent == null) return "Unknown Device";
        String ua = userAgent.toLowerCase();
        if (ua.contains("iphone")) return "iPhone";
        if (ua.contains("ipad")) return "iPad";
        if (ua.contains("android")) return "Android";
        if (ua.contains("macintosh") || ua.contains("mac os")) return "Mac";
        if (ua.contains("windows")) return "Windows PC";
        if (ua.contains("linux")) return "Linux";
        return "Unknown Device";
    }

    public String parseBrowser(String userAgent) {
        if (userAgent == null) return "Unknown Browser";
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg")) return "Edge";
        if (ua.contains("chrome") && !ua.contains("edg")) return "Chrome";
        if (ua.contains("safari") && !ua.contains("chrome")) return "Safari";
        if (ua.contains("firefox")) return "Firefox";
        if (ua.contains("opera") || ua.contains("opr")) return "Opera";
        return "Unknown Browser";
    }

    public String extractClientIp(HttpServletRequest request) {
        if (request == null) return "Unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "Unknown";
    }

    public String createSession(Long userId, String username, HttpServletRequest request) {
        String sessionId = String.valueOf(new SnowflakeIdGenerator().generate(null, null));
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String ipAddress = extractClientIp(request);

        UserSession session = new UserSession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setUsername(username);
        session.setDeviceName(parseDeviceName(userAgent));
        session.setBrowser(parseBrowser(userAgent));
        session.setIpAddress(ipAddress);
        session.setLocation("Unknown");
        session.setLoginTime(LocalDateTime.now());
        session.setLastActive(LocalDateTime.now());

        userSessionRepo.save(session);
        return sessionId;
    }

    public boolean isSessionActive(String sessionId) {
        if (sessionId == null) return false;
        return userSessionRepo.existsById(sessionId);
    }

    public void deleteSession(String sessionId) {
        if (sessionId != null) {
            userSessionRepo.deleteById(sessionId);
        }
    }
}
