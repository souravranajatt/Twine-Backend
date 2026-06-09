package com.loginapp.loginapp.Utils;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.UsersRepo;

@Component
public class AuthUtils {

    @Autowired
    private UsersRepo usersRepo;

    public Users getLoggedUser() {
        String userIdStr = SecurityContextHolder.getContext()
                            .getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);
        return usersRepo.findByUserId(userUid)
                .orElseThrow(() -> new IllegalArgumentException("Something went wrong!"));
    }
}