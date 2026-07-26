package com.loginapp.loginapp.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.loginapp.loginapp.DTO.UserSearchDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.BlockRepo;
import com.loginapp.loginapp.repository.FollowRepo;
import com.loginapp.loginapp.repository.UsersRepo;
import java.util.*;

@Service
public class SearchService {

    private final AuthUtils authUtils;

    private final UsersRepo usersRepo;

    private final BlockRepo blockRepo;

    private final FollowRepo followRepo;

    public SearchService(UsersRepo usersRepo, BlockRepo blockRepo, AuthUtils authUtils, FollowRepo followRepo) {
        this.usersRepo = usersRepo;
        this.blockRepo = blockRepo;
        this.authUtils = authUtils;
        this.followRepo = followRepo;
    }

    // Search users by username or fullname
    public List<UserSearchDTO> searchUsers(String query) {

        // Get Logged User Info
        Users loggedUser = authUtils.getLoggedUser();

        // Clean Query trim, lowercase, remove leading '@'
        String cleanQuery = query.trim().toLowerCase().replaceAll("^@+", "");
        if (cleanQuery.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Now Get Users from the repository based on the query
        List<Users> users = usersRepo.findSearchUsers(cleanQuery, PageRequest.of(0, 20));
        List<Users> filteredUser = new ArrayList<>();
        
        // Now check User is blocked or not 
        Set<Long> blockUserIds = new HashSet<>();
        blockRepo.findBlockedUserIds(loggedUser).forEach(blockUserIds::add); // Add all users blocked by the logged user
        blockRepo.findBlockedByUserIds(loggedUser).forEach(blockUserIds::add); // Add all users who have blocked the logged user
        

        // Filter out blocked users from the search results
        for (Users user : users) {
            if (!blockUserIds.contains(user.getUserId()) &&
                !user.getUserId().equals(loggedUser.getUserId())) {
                filteredUser.add(user);
            }
        }

        // Now get all filtered userid ...
        List<Long> filteredUserIds = new ArrayList<>();
        for(Users user : filteredUser){
            filteredUserIds.add(user.getUserId());
        }

        // Find all following and Folllowers of the logged user
        Set<Long> followingUserIds = new HashSet<>();
        Set<Long> followerUserIds = new HashSet<>();
        followRepo.findFollowerIds(loggedUser, filteredUserIds).forEach(followerUserIds::add);
        followRepo.findFollowingIds(loggedUser, filteredUserIds).forEach(followingUserIds::add);

        PriorityQueue<UserWithScore> pq = new PriorityQueue<>(
            (a, b) -> {
                int scoreCompare = Double.compare(b.score, a.score);
                if (scoreCompare != 0) return scoreCompare;
                int lenCompare = Integer.compare(a.user.getUsername().length(), b.user.getUsername().length());
                if (lenCompare != 0) return lenCompare;
                return a.user.getUsername().compareToIgnoreCase(b.user.getUsername());
            }
        );

        // Score the user ...
        for (Users user : filteredUser) {

            double score = 0.0;

            String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
            String fullname = user.getFullname() != null ? user.getFullname().toLowerCase() : "";
            
            // Validate and Score Username
            if (username.equals(cleanQuery)) {
                score += 3.0;
            } else if (username.startsWith(cleanQuery)) {
                score += 2.5;
            } else {
                // Symbol matching
                String[] tokens = username.split("[._\\-\\s@]+");
                boolean tokenMatched = false;
                for (String token : tokens) {
                    if (token.equals(cleanQuery)) {
                        score += 2.0;
                        tokenMatched = true;
                        break;
                    } else if (token.startsWith(cleanQuery)) {
                        score += 1.5;
                        tokenMatched = true;
                        break;
                    }
                }
                if (!tokenMatched && username.contains(cleanQuery)) {
                    score += 1.0;
                }
            }

            // Validate and Score Fullname
            if (!fullname.isEmpty()) {
                if (fullname.equals(cleanQuery)) {
                    score += 2.5;
                } else if (fullname.startsWith(cleanQuery)) {
                    score += 2.0;
                } else {
                    String[] nameTokens = fullname.split("[._\\-\\s@]+");
                    boolean nameTokenMatched = false;
                    for (String token : nameTokens) {
                        if (token.startsWith(cleanQuery)) {
                            score += 1.5;
                            nameTokenMatched = true;
                            break;
                        }
                    }
                    if (!nameTokenMatched && fullname.contains(cleanQuery)) {
                        score += 1.0;
                    }
                }
            }

            // P1 — Following
            if (followingUserIds.contains(user.getUserId())) score += 3.0;

            // P2 — Verified
            if (user.isVerifyTag()) score += 1.5;

            // P3 — Follower
            if (followerUserIds.contains(user.getUserId())) score += 2.0;

            pq.offer(new UserWithScore(user, score));

        }


        // Convert to DTOs
        List<UserSearchDTO> results = new ArrayList<>();
        while (!pq.isEmpty()) {
            Users user = pq.poll().user;
            UserSearchDTO dto = new UserSearchDTO();
            dto.setUserId(user.getUserId());
            dto.setUsername(user.getUsername());
            dto.setFullname(user.getFullname());
            dto.setVerified(user.isVerifyTag());
            if (user.getUserData() != null) {
                dto.setProfilePhoto(user.getUserData().getProfilePhoto());
            }
            results.add(dto);
        }
        return results;
        
    }

    // Helper class
    private static class UserWithScore {
        Users user;
        double score;
        UserWithScore(Users user, double score) {
            this.user = user;
            this.score = score;
        }
    }
}
