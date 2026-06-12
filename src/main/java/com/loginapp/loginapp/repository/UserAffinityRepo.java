package com.loginapp.loginapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loginapp.loginapp.entity.UserCategoryAffinity;
import com.loginapp.loginapp.entity.Users;

@Repository
public interface UserAffinityRepo extends JpaRepository<UserCategoryAffinity, Long> {

    // For Affinity Scroe Create and Update 
    @Query("""
        SELECT u FROM UserCategoryAffinity u
        WHERE u.user = :user AND u.category = :category
    """)
    UserCategoryAffinity findByUserAndCategory(Users user, String category);

    // Used in Home Feed Algo
    @Query("SELECT u FROM UserCategoryAffinity u WHERE u.user.userId = :userId ORDER BY u.affinityScore DESC")
    List<UserCategoryAffinity> findTopAffinitiesWithScore(@Param("userId") Long userId);
}