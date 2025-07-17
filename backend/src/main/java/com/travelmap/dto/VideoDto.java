package com.travelmap.backend.dto;

import com.travelmap.backend.entity.Video;
import com.travelmap.backend.entity.VisitCountry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class VideoDto {
    private Long id;
    private String title;
    private String videoId;
    private LocalDateTime uploadDate;
    private String thumbnailUrl;
    private String videoUrl;
    private Long viewCount;
    private Long likeCount;
    private String duration;
    private UserDto user;
    private List<VisitCountryDto> visitCountries;
    
    public VideoDto() {}
    
    public VideoDto(Video video) {
        this.id = video.getId();
        this.title = video.getTitle();
        this.videoId = video.getVideoId();
        this.uploadDate = video.getUploadDate();
        this.thumbnailUrl = video.getThumbnailUrl();
        this.videoUrl = video.getVideoUrl();
        this.viewCount = video.getViewCount();
        this.likeCount = video.getLikeCount();
        this.duration = video.getDuration();
        this.user = new UserDto(video.getUser());
        
        if (video.getVisitCountries() != null) {
            this.visitCountries = video.getVisitCountries().stream()
                    .map(VisitCountryDto::new)
                    .collect(Collectors.toList());
        }
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
    
    public String getVideoUrl() {
        return videoUrl;
    }
    
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
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
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public UserDto getUser() {
        return user;
    }
    
    public void setUser(UserDto user) {
        this.user = user;
    }
    
    public List<VisitCountryDto> getVisitCountries() {
        return visitCountries;
    }
    
    public void setVisitCountries(List<VisitCountryDto> visitCountries) {
        this.visitCountries = visitCountries;
    }
} 