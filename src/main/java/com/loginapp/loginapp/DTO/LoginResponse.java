package com.loginapp.loginapp.DTO;

public class LoginResponse {
    private String jwtToken;
    private String message;

    // Getter & Setter 

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getJwtToken() {
        return jwtToken;
    }
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
