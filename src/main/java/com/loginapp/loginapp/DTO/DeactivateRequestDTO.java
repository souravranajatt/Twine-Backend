package com.loginapp.loginapp.DTO;

public class DeactivateRequestDTO {
    private String password;
    private String reason;

    // Geetter and Setter

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    
}

