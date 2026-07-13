package com.travelmap.dto;

import com.travelmap.entity.CountryKeyword;

public class CountryKeywordDto {
    private Long id;
    private String keyword;
    private String countryCode;
    private String countryName;
    private String continent;
    private String countryEmoji;

    public CountryKeywordDto() {}

    public CountryKeywordDto(CountryKeyword entity) {
        this.id = entity.getId();
        this.keyword = entity.getKeyword();
        this.countryCode = entity.getCountryCode();
        this.countryName = entity.getCountryName();
        this.continent = entity.getContinent();
        this.countryEmoji = entity.getCountryEmoji();
    }

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
}
