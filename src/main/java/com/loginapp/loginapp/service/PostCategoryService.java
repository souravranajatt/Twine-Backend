package com.loginapp.loginapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.PostCategoryDTO;
import com.loginapp.loginapp.entity.PostCategories;
import com.loginapp.loginapp.entity.PostsEntity;
import com.loginapp.loginapp.repository.PostCategoryRepo;
import com.loginapp.loginapp.repository.PostRepo;

@Service
public class PostCategoryService {

    @Autowired
    private PostCategoryRepo postCategoryRepo;

    @Autowired
    private PostRepo postRepo;

    @Transactional
    public void saveCategory(Long postId, PostCategoryDTO dto) {
        try {
            PostsEntity freshPost = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

            PostCategories category = new PostCategories();
            category.setPost(freshPost);
            category.setPrimaryCategory(dto.getPrimaryCategory());
            category.setSubCategories(toJsonArray(dto.getSubCategories()));
            category.setTopics(toJsonArray(dto.getTopics()));
            category.setSentiment(
                PostCategories.Sentiment.valueOf(dto.getSentiment())
            );
            category.setContentType(
                PostCategories.ContentType.valueOf(dto.getContentType())
            );
            category.setConfidenceScore(dto.getConfidenceScore());

            postCategoryRepo.save(category);
            System.out.println("Category saved: " + dto.getPrimaryCategory());

        } catch (Exception e) {
            System.out.println("Category save error: " + e.getMessage());
        }
    }

    // New Method 
    private String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i)).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}