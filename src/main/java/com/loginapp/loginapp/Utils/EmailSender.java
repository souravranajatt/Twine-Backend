package com.loginapp.loginapp.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    public static final String OTP_SUBJECT = "Twine - Verify Your Email";

    private final JavaMailSender javaMailSender;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    public EmailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public SimpleMailMessage buildOtpMessage(String to, String fullname, String otpCode) {
        var message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(OTP_SUBJECT);
        message.setText(
            "Hi " + fullname + ",\n\n" +
            "Your OTP code is: " + otpCode + "\n\n" +
            "This code is valid for " + otpExpiryMinutes + " minutes.\n" +
            "Do not share this with anyone.\n\n" +
            "- Twine Team"
        );
        return message;
    }

    public boolean sendEmail(SimpleMailMessage message) {
        try {
            javaMailSender.send(message);
            return true;
        } catch (MailException ex) {
            return false;
        }
    }
}
