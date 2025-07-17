package com.travelmap.backend.dto;

import com.travelmap.backend.entity.VisitCountry;

public class VisitCountryDto {
    private Long id;
    private String countryCode;
    private String countryName;
    private String countryEmoji;
    private String continent;
    private String detectionMethod;
    private Double confidenceScore;
    private Integer visitOrder;
    
    public VisitCountryDto() {}
    
    public VisitCountryDto(VisitCountry visitCountry) {
        this.id = visitCountry.getId();
        this.countryCode = visitCountry.getCountryCode();
        this.countryName = visitCountry.getCountryName();
        this.countryEmoji = visitCountry.getCountryEmoji();
        this.continent = visitCountry.getContinent();
        this.detectionMethod = visitCountry.getDetectionMethod();
        this.confidenceScore = visitCountry.getConfidenceScore();
        this.visitOrder = visitCountry.getVisitOrder();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public String getCountryName() {
        return countryName;
    }
    
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
    
    public String getCountryEmoji() {
        return countryEmoji;
    }
    
    public void setCountryEmoji(String countryEmoji) {
        this.countryEmoji = countryEmoji;
    }
    
    public String getContinent() {
        return continent;
    }
    
    public void setContinent(String continent) {
        this.continent = continent;
    }
    
    public String getDetectionMethod() {
        return detectionMethod;
    }
    
    public void setDetectionMethod(String detectionMethod) {
        this.detectionMethod = detectionMethod;
    }
    
    public Double getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public Integer getVisitOrder() {
        return visitOrder;
    }
    
    public void setVisitOrder(Integer visitOrder) {
        this.visitOrder = visitOrder;
    }
} 