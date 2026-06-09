package com.loginapp.loginapp.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.entity.PostCategories;
import com.loginapp.loginapp.entity.PostCategories.Sentiment;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.entity.UserCategoryAffinity;
import com.loginapp.loginapp.entity.Users;
import com.loginapp.loginapp.repository.UserAffinityRepo;

@Service
@Transactional
public class AffinityService {

    public static final float LIKE_WEIGHT    = 0.30f;
    public static final float SAVE_WEIGHT    = 0.50f;
    public static final float COMMENT_WEIGHT = 0.45f;
    public static final float SHARE_WEIGHT   = 0.60f;
    public static final float VIEW_WEIGHT    = 0.05f;
    public static final float UNLIKE_WEIGHT  = -0.20f;
    public static final float UNSAVE_WEIGHT  = -0.30f;

    public static final float MIN_CONFIDENCE = 0.6f;

    @Autowired
    private UserAffinityRepo userAffinityRepo;
    
    
    // On Like 
    public void updateAffinityOnLike(Users user, PostsEntity post){
        calculateAffinityScore(user, post, LIKE_WEIGHT);
    }

    // On Unlike
    public void updateAffinityOnUnlike(Users user, PostsEntity post){
        calculateAffinityScore(user, post, UNLIKE_WEIGHT);
    }

    // On Comment
    public void updateAffinityOnComment(Users user, PostsEntity post){
        calculateAffinityScore(user, post, COMMENT_WEIGHT);
    }

    // On Save
    public void updateAffinityOnSave(Users user, PostsEntity post){
        calculateAffinityScore(user, post, SAVE_WEIGHT);
    }

    // On Unsave
    public void updateAffinityOnUnsave(Users user, PostsEntity post){
        calculateAffinityScore(user, post, UNSAVE_WEIGHT);
    }

    // On View
    public void updateAffinityOnView(Users user, PostsEntity post){
        calculateAffinityScore(user, post, VIEW_WEIGHT);
    }


    // Calculate Affinity Score
    public void calculateAffinityScore(Users user, PostsEntity post, float weight) {

        // Check null and confidence score
        PostCategories postCat = post.getPostCategories();
        if(postCat == null ||
        postCat.getPrimaryCategory() == null ||
        postCat.getConfidenceScore() == null ||      
        postCat.getConfidenceScore() < MIN_CONFIDENCE) { 
            return;
        }

        // Set Sentiment Multiplier
        float sentimentMultiplier = 1.0f;
        if(postCat.getSentiment() == Sentiment.NEGATIVE){
            sentimentMultiplier = 0.5f;
        } else if(postCat.getSentiment() == Sentiment.POSITIVE){
            sentimentMultiplier = 1.5f;
        }

        // Find and Check existing affinity
        UserCategoryAffinity affinity = userAffinityRepo
                .findByUserAndCategory(user, postCat.getPrimaryCategory());

        if(affinity == null){
            affinity = new UserCategoryAffinity();
            affinity.setUser(user);
            affinity.setCategory(postCat.getPrimaryCategory());
            affinity.setAffinityScore(0.0f);
            affinity.setInteractionCount(0);
        }

        // Calculate final score
        float finalScore = weight * sentimentMultiplier * postCat.getConfidenceScore();
        float oldScore = affinity.getAffinityScore();
        float newScore = (oldScore * 0.9f) + (finalScore * 0.1f);
        float finalAffinityScore = Math.min(1.0f, Math.max(0.0f, newScore));

        // Update
        affinity.setAffinityScore(finalAffinityScore);
        affinity.setInteractionCount(affinity.getInteractionCount() + 1);
        affinity.setLastInteractedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        userAffinityRepo.save(affinity); // Save to DB
    }

}
