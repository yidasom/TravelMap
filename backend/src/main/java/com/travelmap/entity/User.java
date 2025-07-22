package com.travelmap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "유튜버 이름은 필수입니다")
    @Size(max = 100, message = "유튜버 이름은 100자를 초과할 수 없습니다")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Size(max = 100, message = "검색 쿼리는 100자를 초과할 수 없습니다")
    @Column(name = "search_query", length = 100)
    private String searchQuery;
    
    @Column(name = "youtube_channel_id", unique = true, length = 50)
    private String youtubeChannelId;
    
    @Column(name = "channel_url", length = 255)
    private String channelUrl;
    
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "subscriber_count")
    private Long subscriberCount;
    
    @Column(name = "total_view_count")
    private Long totalViewCount;
    
    @Column(name = "total_video_count")
    private Long totalVideoCount;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Video> videos;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public User() {}
    
    public User(String name, String searchQuery, String youtubeChannelId) {
        this.name = name;
        this.searchQuery = searchQuery;
        this.youtubeChannelId = youtubeChannelId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSearchQuery() {
        return searchQuery;
    }
    
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
    
    public String getYoutubeChannelId() {
        return youtubeChannelId;
    }
    
    public void setYoutubeChannelId(String youtubeChannelId) {
        this.youtubeChannelId = youtubeChannelId;
    }
    
    public String getChannelUrl() {
        return channelUrl;
    }
    
    public void setChannelUrl(String channelUrl) {
        this.channelUrl = channelUrl;
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getSubscriberCount() {
        return subscriberCount;
    }
    
    public void setSubscriberCount(Long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }
    
    public Long getTotalViewCount() {
        return totalViewCount;
    }
    
    public void setTotalViewCount(Long totalViewCount) {
        this.totalViewCount = totalViewCount;
    }
    
    public Long getTotalVideoCount() {
        return totalVideoCount;
    }
    
    public void setTotalVideoCount(Long totalVideoCount) {
        this.totalVideoCount = totalVideoCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Video> getVideos() {
        return videos;
    }
    
    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", searchQuery='" + searchQuery + '\'' +
                ", youtubeChannelId='" + youtubeChannelId + '\'' +
                '}';
    }
} 