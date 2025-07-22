package com.travelmap.dto;

import java.util.List;

public class MapDataDto {
    private List<CountryDataDto> countries;
    
    public MapDataDto() {}
    
    public MapDataDto(List<CountryDataDto> countries) {
        this.countries = countries;
    }
    
    public List<CountryDataDto> getCountries() {
        return countries;
    }
    
    public void setCountries(List<CountryDataDto> countries) {
        this.countries = countries;
    }
    
    public static class CountryDataDto {
        private String countryCode;
        private String countryName;
        private String countryEmoji;
        private String continent;
        private Long visitCount;
        private Long youtuberCount;
        private List<UserDto> youtubers;
        
        public CountryDataDto() {}
        
        public CountryDataDto(String countryCode, String countryName, String countryEmoji, 
                             String continent, Long visitCount, Long youtuberCount) {
            this.countryCode = countryCode;
            this.countryName = countryName;
            this.countryEmoji = countryEmoji;
            this.continent = continent;
            this.visitCount = visitCount;
            this.youtuberCount = youtuberCount;
        }
        
        // Getters and Setters
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
        
        public Long getVisitCount() {
            return visitCount;
        }
        
        public void setVisitCount(Long visitCount) {
            this.visitCount = visitCount;
        }
        
        public Long getYoutuberCount() {
            return youtuberCount;
        }
        
        public void setYoutuberCount(Long youtuberCount) {
            this.youtuberCount = youtuberCount;
        }
        
        public List<UserDto> getYoutubers() {
            return youtubers;
        }
        
        public void setYoutubers(List<UserDto> youtubers) {
            this.youtubers = youtubers;
        }
    }
} 