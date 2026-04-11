package com.loginapp.loginapp.DTO;

import java.util.List;

public class PostCategoryDTO {

    private String primaryCategory;
    private List<String> subCategories;
    private List<String> topics;
    private String sentiment;
    private String contentType;
    private Float confidenceScore;

    // Getters & Setters
    public String getPrimaryCategory() { return primaryCategory; }
    public void setPrimaryCategory(String primaryCategory) { this.primaryCategory = primaryCategory; }

    public List<String> getSubCategories() { return subCategories; }
    public void setSubCategories(List<String> subCategories) { this.subCategories = subCategories; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }

    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Float getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Float confidenceScore) { this.confidenceScore = confidenceScore; }
}