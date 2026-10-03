package com.loginapp.loginapp.Utils;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.loginapp.loginapp.DTO.PostFetchDTO;
import com.loginapp.loginapp.DTO.TaggingResult;
import com.loginapp.loginapp.entity.PostMedia;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.Users;

@Component
public class PostDTOMapper {

    private final SocialFilterHelper socialFilterHelper;

    public PostDTOMapper(SocialFilterHelper socialFilterHelper) {
        this.socialFilterHelper = socialFilterHelper;
    }

    // Convert post entity to PostFetchDTO
    public PostFetchDTO toDTO(PostsEntity post, Users loggedUser,
                              Set<Long> blockedIds,
                              Set<Long> likedPostIds,
                              Set<Long> savedPostIds) {

        PostFetchDTO dto = new PostFetchDTO();

        // Basic post details
        dto.setFetchPostId(String.valueOf(post.getPostId()));
        dto.setFetchFileName(post.getFileName());
        dto.setFetchPostCaption(post.getPostCaption());
        dto.setFetchPostLocation(post.getPostLocation());
        dto.setFetchUploadAt(post.getUploadAt());

        // User info
        Users postOwner = post.getUserpost();
        if (postOwner != null) {
            dto.setUserId(String.valueOf(postOwner.getUserId()));
            dto.setUsername(postOwner.getUsername());
            dto.setFullname(postOwner.getFullname());
            dto.setFetchVerified(postOwner.isVerifyTag());
            if (postOwner.getUserData() != null) {
                dto.setProfileImage(postOwner.getUserData().getProfilePhoto());
            }
        }

        // Tagged users
        List<TaggingResult> taggedUsers = socialFilterHelper.resolveTaggedUsers(
            post.getTaggedUsers(), blockedIds
        );
        dto.setFetchTaggedUsers(taggedUsers);

        // Timeline user
        if (post.getTimelineUser() != null) {
            dto.setFetchTimelineUser(String.valueOf(post.getTimelineUser()));
        }

        // Post counts
        dto.setLikeCount(post.getLikeCount() != null ? post.getLikeCount() : 0L);
        dto.setCommentCount(post.getCommentCount() != null ? post.getCommentCount() : 0L);
        dto.setViewCount(post.getViewCount() != null ? post.getViewCount() : 0L);
        dto.setSaveCount(post.getSaveCount() != null ? post.getSaveCount() : 0L);

        // Post permissions
        dto.setCommentEnable(post.getCommentEnabled() != null ? post.getCommentEnabled() : true);
        dto.setShareEnable(post.getShareEnabled() != null ? post.getShareEnabled() : true);
        dto.setLikeVisible(post.getLikeVisible() != null ? post.getLikeVisible() : true);

        // Media info
        PostMedia media = post.getPostMedia();
        if (media != null) {
            dto.setWidth(media.getWidth());
            dto.setHeight(media.getHeight());
            dto.setDuration(media.getDuration());
            if (media.getPostType() != null) {
                dto.setPostType(media.getPostType().name());
            }
        }

        // Like, save and own post flags
        dto.setLikedByCurrentUser(likedPostIds != null && likedPostIds.contains(post.getPostId()));
        dto.setSavedByCurrentUser(savedPostIds != null && savedPostIds.contains(post.getPostId()));
        dto.setOwnPost(postOwner != null && loggedUser != null && postOwner.getUserId().equals(loggedUser.getUserId()));

        return dto;
    }
}
