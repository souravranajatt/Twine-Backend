package com.loginapp.loginapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.loginapp.loginapp.entity.FollowUser;
import com.loginapp.loginapp.entity.Users;

public interface FollowRepo extends JpaRepository<FollowUser, Long> {
    Optional<FollowUser> findByFollowerAndFollowing(Users follower, Users following);

    // Returns true if logged user follows searched user AND statusVal = 1
    boolean existsByFollower_UserIdAndFollowing_UserId(Long followerId, Long followingId);
    
    // Count followers (only active)
    long countByFollowing_UserId(Long userId);

    // Count following (only active)
    long countByFollower_UserId(Long userId);
}
