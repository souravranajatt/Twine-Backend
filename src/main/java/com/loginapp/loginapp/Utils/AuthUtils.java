package com.loginapp.loginapp.Utils;

import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.UsersRepo;

@Component
public class AuthUtils {

    private final UsersRepo usersRepo;

    AuthUtils(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    // Get Current Logged User
    public Users getLoggedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null 
                || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalArgumentException("Token Expired!");
        }

        String userIdStr = authentication.getName();
        Long userUid;
        try {
            userUid = Long.parseLong(userIdStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Token Expired!");
        }

        Users user = usersRepo.findByUserId(userUid)
                .orElseThrow(() -> new IllegalArgumentException("Something went wrong!"));

        if (user.isStatusDeleted()) {
            throw new IllegalArgumentException("Something went wrong!");
        }

        if(user.isStatusSuspend()){
            throw new IllegalArgumentException("Something went wrong!");
        }

        return user;
    }

    // Get Current User Session Id
    public String getCurrentSessionId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof String sessionId) {
            return sessionId;
        }
        return null;
    }
}