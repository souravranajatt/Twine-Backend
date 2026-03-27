package com.loginapp.loginapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.loginapp.loginapp.entity.FollowRequestTable;
import com.loginapp.loginapp.entity.Users;

public interface FollowRequestRepo extends JpaRepository<FollowRequestTable, Long>{
    Optional<FollowRequestTable> findBySenderIdAndReceiverId(Users sender, Users receiver);

    Boolean existsBySenderIdAndReceiverId(Users sender, Users receiver);
}
