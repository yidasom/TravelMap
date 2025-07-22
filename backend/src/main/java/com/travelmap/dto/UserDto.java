package com.travelmap.dto;

import com.travelmap.entity.User;

public class UserDto {
    private Long id;
    private String name;
    private String searchQuery;
    private String youtubeChannelId;
    private String channelUrl;
    private String profileImageUrl;
    private Long subscriberCount;
    private Long totalVideoCount;
    
    public UserDto() {}
    
    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.searchQuery = user.getSearchQuery();
        this.youtubeChannelId = user.getYoutubeChannelId();
        this.channelUrl = user.getChannelUrl();
        this.profileImageUrl = user.getProfileImageUrl();
        this.subscriberCount = user.getSubscriberCount();
        this.totalVideoCount = user.getTotalVideoCount();
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
    
    public Long getSubscriberCount() {
        return subscriberCount;
    }
    
    public void setSubscriberCount(Long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }
    
    public Long getTotalVideoCount() {
        return totalVideoCount;
    }
    
    public void setTotalVideoCount(Long totalVideoCount) {
        this.totalVideoCount = totalVideoCount;
    }
} 