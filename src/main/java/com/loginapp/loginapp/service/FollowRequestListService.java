package com.loginapp.loginapp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.loginapp.loginapp.DTO.FollowRequestListDTO;
import com.loginapp.loginapp.Utils.AuthUtils;
import com.loginapp.loginapp.entity.FollowRequestTable;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.BlockRepo;
import com.loginapp.loginapp.repository.FollowRequestRepo;

@Service
public class FollowRequestListService {

    private static final String DEFAULT_AVATAR = "https://res.cloudinary.com/dgoqiyoeq/image/upload/v1776851796/Twine_DefaultNullImage_qosaiv.png";

    private final FollowRequestRepo followRequestRepo;
    private final AuthUtils authUtils;
    private final BlockRepo blockRepo;

    public FollowRequestListService(FollowRequestRepo followRequestRepo, AuthUtils authUtils, BlockRepo blockRepo) {
        this.followRequestRepo = followRequestRepo;
        this.authUtils = authUtils;
        this.blockRepo = blockRepo;
    }

    // Service Logic for follow requests
    public List<FollowRequestListDTO> handleFollowRequest(int page) {
        return handleFollowRequest(page, 15);
    }

    // service method to fetch paginated incoming follow requests
    public List<FollowRequestListDTO> handleFollowRequest(int page, int size) {

        // Get LoggedUser Data
        Users loggedUser = authUtils.getLoggedUser();
        if (loggedUser == null) {
            return Collections.emptyList();
        }

        // Fetch blocked user IDs 
        Set<Long> iBlocked = blockRepo.findBlockedUserIds(loggedUser);
        Set<Long> blockedMe = blockRepo.findBlockedByUserIds(loggedUser);

        // Fetch paginated follow request records from database
        Pageable pageable = PageRequest.of(page, size);
        List<FollowRequestTable> followRequestEntities = followRequestRepo.findFollowRequestsForUser(loggedUser, pageable);

        if (followRequestEntities == null || followRequestEntities.isEmpty()) {
            return Collections.emptyList();
        }

        // Map entities to DTOs while filtering out blocked/inactive users
        List<FollowRequestListDTO> resultList = new ArrayList<>();

        for (FollowRequestTable request : followRequestEntities) {
            Users sender = request.getSenderId();

            // Skip if sender is null, deleted, suspended, or involved in a block relationship
            if (sender == null || sender.isStatusDeleted() || sender.isStatusSuspend()) {
                continue;
            }

            Long senderId = sender.getUserId();
            if (iBlocked.contains(senderId) || blockedMe.contains(senderId)) {
                continue;
            }

            // DTO response object
            FollowRequestListDTO dto = new FollowRequestListDTO();
            dto.setUserId(String.valueOf(sender.getUserId()));
            dto.setUsername(sender.getUsername());
            dto.setName(sender.getFullname());
            dto.setVerify(sender.isVerifyTag());
            dto.setRequestedOn(request.getRequestedOn());

            // Get user profile picture
            if (sender.getUserData() != null 
                    && sender.getUserData().getProfilePhoto() != null 
                    && !sender.getUserData().getProfilePhoto().trim().isEmpty() 
                    && !sender.getUserData().getProfilePhoto().equalsIgnoreCase("null")) {
                dto.setProfilePicture(sender.getUserData().getProfilePhoto());
            } else {
                dto.setProfilePicture(DEFAULT_AVATAR);
            }

            resultList.add(dto);
        }

        return resultList;
    }
}
