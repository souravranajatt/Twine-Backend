package com.loginapp.loginapp.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.Users;
import java.util.Optional;
import java.util.List;

public interface UsersRepo extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username);
    Optional<Users> findByEmail(String email);
    Optional<Users> findByUserId(Long userId);
    Optional<Users> findByMobileNumber(String mobileNumber);


    // Search Users 
    @Query("""
        SELECT u FROM Users u
        WHERE (
            LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        AND u.statusDeleted = false
        AND u.statusSuspend = false
    """)
    List<Users> findSearchUsers(@Param("query") String query, Pageable pageable);

    // Search User for Tagging 
    @Query("""
        SELECT u FROM Users u
        LEFT JOIN FETCH u.setting
        WHERE (
            LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        AND u.statusDeleted = false
        AND u.statusSuspend = false
    """)
    List<Users> findSearchUsersForTagging(@Param("query") String query, Pageable pageable);

    // Find User for Tagged
    @Query("""
        SELECT u FROM Users u
        WHERE u.userId IN :userIds
        AND u.statusDeleted = false
        AND u.statusSuspend = false
    """)
    List<Users> findTaggedUsersByIds(@Param("userIds") List<String> userIds);


}
