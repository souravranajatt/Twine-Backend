package com.loginapp.loginapp.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.loginapp.loginapp.DTO.FollowListFetchDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.BlockRepo;
import com.loginapp.loginapp.repository.FollowRepo;
import com.loginapp.loginapp.repository.UsersRepo;

@Service
public class UserSuggestionService {
    
    private final UsersRepo usersRepo;
    private final FollowRepo followRepo;
    private final BlockRepo blockRepo;
    private final AuthUtils authUtils;

    public UserSuggestionService(UsersRepo usersRepo, FollowRepo followRepo, BlockRepo blockRepo, AuthUtils authUtils) {
        this.usersRepo = usersRepo;
        this.followRepo = followRepo;
        this.blockRepo = blockRepo;
        this.authUtils = authUtils;
    }

    // Default overloaded suggestion list
    public List<FollowListFetchDTO> suggestionList() {
        return suggestionList(0, 10);
    }

    // Paginated Suggestion List Logic
    public List<FollowListFetchDTO> suggestionList(int page, int size) {
        
        // Get Logged In User
        Users loggedUser = authUtils.getLoggedUser();
        if (loggedUser == null) {
            return Collections.emptyList();
        }

        // Fetch users loggedUser already follows
        List<Users> myFollowing = followRepo.findFollowingUsers(loggedUser);
        Set<Long> myFollowingIds = myFollowing.stream()
                .map(Users::getUserId)
                .collect(Collectors.toSet());
        myFollowingIds.add(loggedUser.getUserId()); // Exclude self

        // Fetch Blocked User IDs (both directions)
        Set<Long> iBlocked = blockRepo.findBlockedUserIds(loggedUser);
        Set<Long> blockedMe = blockRepo.findBlockedByUserIds(loggedUser);

        LinkedHashMap<Long, Users> candidateMap = new LinkedHashMap<>();

        //  2-Hop Graph Traversal (Mutual Friends)
        if (!myFollowing.isEmpty()) {
            Pageable pageable = PageRequest.of(page, Math.max(size * 2, 20));
            List<Object[]> mutualResults = followRepo.findSuggestedUsersByMutuals(loggedUser, myFollowing, pageable);
            for (Object[] row : mutualResults) {
                if (row[0] instanceof Users candidate) {
                    if (!myFollowingIds.contains(candidate.getUserId())
                            && !iBlocked.contains(candidate.getUserId())
                            && !blockedMe.contains(candidate.getUserId())
                            && !candidate.isStatusDeleted()
                            && !candidate.isStatusSuspend()) {
                        candidateMap.put(candidate.getUserId(), candidate);
                    }
                }
            }
        }

        // Fallback to active recent users 
        if (candidateMap.size() < size) {
            Pageable fallbackPageable = PageRequest.of(page, Math.max(size * 3, 30));
            List<Users> recentUsers;
            if (!myFollowing.isEmpty()) {
                recentUsers = usersRepo.findRecentUsersExcludingFollowing(loggedUser, myFollowing, fallbackPageable);
            } else {
                recentUsers = usersRepo.findRecentUsersForSuggestion(loggedUser, fallbackPageable);
            }

            for (Users user : recentUsers) {
                if (!myFollowingIds.contains(user.getUserId())
                        && !iBlocked.contains(user.getUserId())
                        && !blockedMe.contains(user.getUserId())
                        && !user.isStatusDeleted()
                        && !user.isStatusSuspend()) {
                    candidateMap.putIfAbsent(user.getUserId(), user);
                }
            }
        }

        if (candidateMap.isEmpty()) {
            return Collections.emptyList();
        }

        // check who follows the logged-in user
        List<Long> candidateIds = new ArrayList<>(candidateMap.keySet());
        Set<Long> theyFollowMe = followRepo.findFollowerIds(loggedUser, candidateIds);

        // Convert to DTO
        List<FollowListFetchDTO> fetchList = new ArrayList<>();
        for (Users candidate : candidateMap.values()) {
            FollowListFetchDTO dto = new FollowListFetchDTO();
            dto.setUserId(String.valueOf(candidate.getUserId()));
            dto.setUsername(candidate.getUsername());
            dto.setName(candidate.getFullname());
            dto.setVerify(candidate.isVerifyTag());
            dto.setIsPrivate(candidate.isStatusPrivate());

            if (candidate.getUserData() != null && candidate.getUserData().getProfilePhoto() != null && !candidate.getUserData().getProfilePhoto().equals("null")) {
                dto.setProfilePicture(candidate.getUserData().getProfilePhoto());
            } else {
                dto.setProfilePicture("https://res.cloudinary.com/dgoqiyoeq/image/upload/v1776851796/Twine_DefaultNullImage_qosaiv.png");
            }

            dto.setFollowsYou(theyFollowMe.contains(candidate.getUserId()));
            dto.setFollowedByMe(false);
            dto.setIsMe(false);

            fetchList.add(dto);
            if (fetchList.size() >= size) {
                break;
            }
        }

        return fetchList;
    }
}
