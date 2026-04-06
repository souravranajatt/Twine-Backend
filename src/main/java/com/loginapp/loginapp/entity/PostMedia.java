package com.loginapp.loginapp.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_media")
public class PostMedia {
    
    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(
        name = "snowflake",
        strategy = "com.loginapp.loginapp.Utils.SnowflakeIdGenerator"
    )
    @Column(name = "media_id", nullable = false, unique = true)
    private Long mediaId;


    @OneToOne
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private PostsEntity post;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 20)
    private PostType postType;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    public enum PostType { IMAGE, VIDEO, REEL }

    @Column(name = "duration")
    private Integer duration;

    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public PostsEntity getPost() {
        return post;
    }

    public void setPost(PostsEntity post) {
        this.post = post;
    }

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    


}
