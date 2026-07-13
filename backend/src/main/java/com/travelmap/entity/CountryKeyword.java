package com.travelmap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 영상 제목에서 국가를 감지하는 데 쓰는 키워드 하나(예: "한국", "korea" -> KR).
 * DB에 저장해두고 관리자 API로 추가/삭제할 수 있어서, 새 국가가 나올 때마다 코드를 고칠 필요가 없다.
 */
@Entity
@Table(name = "country_keywords", uniqueConstraints = @UniqueConstraint(columnNames = "keyword"))
public class CountryKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String keyword;

    @NotBlank
    @Column(name = "country_code", nullable = false, length = 5)
    private String countryCode;

    @NotBlank
    @Column(name = "country_name", nullable = false, length = 100)
    private String countryName;

    @Column(length = 50)
    private String continent;

    @Column(name = "country_emoji", length = 10)
    private String countryEmoji;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public CountryKeyword() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getContinent() { return continent; }
    public void setContinent(String continent) { this.continent = continent; }

    public String getCountryEmoji() { return countryEmoji; }
    public void setCountryEmoji(String countryEmoji) { this.countryEmoji = countryEmoji; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
