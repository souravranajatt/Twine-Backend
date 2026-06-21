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

    public Users getLoggedUser() {
        String userIdStr = SecurityContextHolder.getContext()
                                .getAuthentication().getName();
        Long userUid = Long.parseLong(userIdStr);

        Users user = usersRepo.findByUserId(userUid)
                .orElseThrow(() -> new IllegalArgumentException("Something went wrong!"));

        if (user.isStatusDeleted()) {
            throw new IllegalArgumentException("Something went wrong!");
        }

        return user;
    }
}