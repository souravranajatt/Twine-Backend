package com.loginapp.loginapp.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loginapp.loginapp.entity.FollowUser;
import com.loginapp.loginapp.entity.Users;


public interface FollowRepo extends JpaRepository<FollowUser, Long> {
    Optional<FollowUser> findByFollowerAndFollowing(Users follower, Users following);

    // Returns true if logged user follows searched user 
    boolean existsByFollower_UserIdAndFollowing_UserId(Long followerId, Long followingId);
    
    // Count followers and following for a user
    @Query("SELECT COUNT(f) FROM FollowUser f WHERE f.following.userId = :userId AND f.follower.statusDeleted = false")
    long countByFollowing_UserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM FollowUser f WHERE f.follower.userId = :userId AND f.following.statusDeleted = false")
    long countByFollower_UserId(@Param("userId") Long userId);

    @Query("SELECT f.following FROM FollowUser f WHERE f.follower = :user AND f.following.statusDeleted = false ")
    List<Users> findFollowingUsers(Users user);

    @Query("SELECT f.follower FROM FollowUser f WHERE f.following = :user AND f.follower.statusDeleted = false ")
    List<Users> findFollowerUsers(Users user, Pageable pageable);

    // Delete follow relationship by user ids
    void deleteByFollower_UserIdAndFollowing_UserId(Long followerId, Long followingId);

	boolean existsByFollowerAndFollowing(Users userOne, Users userTwo);

    @Query("""
        SELECT f.follower.userId FROM FollowUser f
        WHERE f.following = :user
        AND f.follower.userId IN :ids
        AND f.follower.statusDeleted = false
    """)
    Set<Long> findFollowerIds(@Param("user") Users user, @Param("ids") List<Long> ids);

    @Query("""
        SELECT f.following.userId FROM FollowUser f
        WHERE f.follower = :user
        AND f.following.userId IN :ids
        AND f.following.statusDeleted = false
    """)
    Set<Long> findFollowingIds(@Param("user") Users user, @Param("ids") List<Long> ids);
}
