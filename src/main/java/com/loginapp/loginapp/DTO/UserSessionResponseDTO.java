package com.loginapp.loginapp.DTO;

import java.time.LocalDateTime;

public class UserSessionResponseDTO {

    private String sessionId;
    private String deviceName;
    private String browser;
    private String ipAddress;
    private String location;
    private LocalDateTime loginTime;
    private LocalDateTime lastActive;
    private boolean currentDevice;

    // Getters & Setters

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }

    public boolean isCurrentDevice() { return currentDevice; }
    public void setCurrentDevice(boolean currentDevice) { this.currentDevice = currentDevice; }
}
