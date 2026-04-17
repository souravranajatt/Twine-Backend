package com.loginapp.loginapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.loginapp.loginapp.entity.UserCategoryAffinity;

@Repository
public interface UserAffinityRepo extends JpaRepository<UserCategoryAffinity, Long> {
    @Query("""
        SELECT u.category FROM UserCategoryAffinity u
        WHERE u.user.userId = :userId
        ORDER BY u.affinityScore DESC
    """)
    List<String> findTopCategories(Long userId);
}