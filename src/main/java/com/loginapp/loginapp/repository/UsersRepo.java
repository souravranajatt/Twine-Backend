package com.loginapp.loginapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.loginapp.loginapp.entity.Users;
import java.util.Optional;

public interface UsersRepo extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    Optional<Users> findByUserId(Long userId);

}
