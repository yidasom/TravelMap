package com.travelmap.dto;

import com.travelmap.entity.CityKeyword;

public class CityKeywordDto {
    private Long id;
    private String keyword;
    private String cityName;
    private Double latitude;
    private Double longitude;
    private String countryCode;
    private String countryName;
    private String continent;
    private String countryEmoji;

    public CityKeywordDto() {}

    public CityKeywordDto(CityKeyword entity) {
        this.id = entity.getId();
        this.keyword = entity.getKeyword();
        this.cityName = entity.getCityName();
        this.latitude = entity.getLatitude();
        this.longitude = entity.getLongitude();
        this.countryCode = entity.getCountryCode();
        this.countryName = entity.getCountryName();
        this.continent = entity.getContinent();
        this.countryEmoji = entity.getCountryEmoji();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getContinent() { return continent; }
    public void setContinent(String continent) { this.continent = continent; }

    public String getCountryEmoji() { return countryEmoji; }
    public void setCountryEmoji(String countryEmoji) { this.countryEmoji = countryEmoji; }
}
