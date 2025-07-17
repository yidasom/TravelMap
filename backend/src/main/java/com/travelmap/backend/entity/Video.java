package com.travelmap.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "videos")
public class Video {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "영상 제목은 필수입니다")
    @Size(max = 500, message = "영상 제목은 500자를 초과할 수 없습니다")
    @Column(nullable = false, length = 500)
    private String title;
    
    @NotBlank(message = "영상 ID는 필수입니다")
    @Column(name = "video_id", nullable = false, unique = true, length = 20)
    private String videoId;
    
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;
    
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "view_count")
    private Long viewCount;
    
    @Column(name = "like_count")
    private Long likeCount;
    
    @Column(name = "comment_count")
    private Long commentCount;
    
    @Column(name = "duration")
    private String duration;
    
    @Column(name = "video_url", length = 255)
    private String videoUrl;
    
    @Column(name = "processed")
    private Boolean processed = false;
    
    @Column(name = "ocr_processed")
    private Boolean ocrProcessed = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VisitCountry> visitCountries;
    
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
    public Video() {}
    
    public Video(String title, String videoId, User user) {
        this.title = title;
        this.videoId = videoId;
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getVideoId() {
        return videoId;
    }
    
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
    
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }
    
    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
    
    public Long getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    
    public Long getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getVideoUrl() {
        return videoUrl;
    }
    
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
    
    public Boolean getProcessed() {
        return processed;
    }
    
    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }
    
    public Boolean getOcrProcessed() {
        return ocrProcessed;
    }
    
    public void setOcrProcessed(Boolean ocrProcessed) {
        this.ocrProcessed = ocrProcessed;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<VisitCountry> getVisitCountries() {
        return visitCountries;
    }
    
    public void setVisitCountries(List<VisitCountry> visitCountries) {
        this.visitCountries = visitCountries;
    }
    
    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", videoId='" + videoId + '\'' +
                ", uploadDate=" + uploadDate +
                '}';
    }
} 