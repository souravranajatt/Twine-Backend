package com.loginapp.loginapp.service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.loginapp.loginapp.DTO.FollowListFetchDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.UsersRepo;

@Service
public class UserSuggestionService {
    
    private final UsersRepo usersRepo;

    private final AuthUtils authUtils;

    UserSuggestionService(UsersRepo usersRepo, AuthUtils authUtils) {
        this.usersRepo = usersRepo;
        this.authUtils = authUtils;
    }

    // Suggestion List Logic
    // public List<FollowListFetchDTO> suggestionList(){
        
    //     // Get Logged In User ID
    //     Users loggedUser = authUtils.getLoggedUser();

    //     // 5 Factor of Ranking , Location, Intrest, Mutual, Most Viewed
    // }
}
