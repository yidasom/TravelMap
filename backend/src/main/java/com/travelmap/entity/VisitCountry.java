package com.travelmap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "visit_countries")
public class VisitCountry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "국가 코드는 필수입니다")
    @Size(max = 5, message = "국가 코드는 5자를 초과할 수 없습니다")
    @Column(name = "country_code", nullable = false, length = 5)
    private String countryCode;
    
    @NotBlank(message = "국가명은 필수입니다")
    @Size(max = 100, message = "국가명은 100자를 초과할 수 없습니다")
    @Column(name = "country_name", nullable = false, length = 100)
    private String countryName;
    
    @Column(name = "country_emoji", length = 10)
    private String countryEmoji;
    
    @Column(name = "continent", length = 50)
    private String continent;
    
    @Column(name = "detection_method", length = 20)
    private String detectionMethod; // "TITLE_EMOJI", "OCR", "MANUAL"
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "visit_order")
    private Integer visitOrder;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;
    
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
    public VisitCountry() {}
    
    public VisitCountry(String countryCode, String countryName, Video video) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.video = video;
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
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Integer getVisitOrder() {
        return visitOrder;
    }
    
    public void setVisitOrder(Integer visitOrder) {
        this.visitOrder = visitOrder;
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
    
    public Video getVideo() {
        return video;
    }
    
    public void setVideo(Video video) {
        this.video = video;
    }
    
    @Override
    public String toString() {
        return "VisitCountry{" +
                "id=" + id +
                ", countryCode='" + countryCode + '\'' +
                ", countryName='" + countryName + '\'' +
                ", countryEmoji='" + countryEmoji + '\'' +
                '}';
    }
} 