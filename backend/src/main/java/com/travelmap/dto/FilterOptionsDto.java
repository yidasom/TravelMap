package com.travelmap.dto;

import java.util.List;

public class FilterOptionsDto {
    private List<UserDto> users;
    private List<String> countries;
    private List<String> years;
    private List<String> continents;
    
    public FilterOptionsDto() {}
    
    public FilterOptionsDto(List<UserDto> users, List<String> countries, 
                           List<String> years, List<String> continents) {
        this.users = users;
        this.countries = countries;
        this.years = years;
        this.continents = continents;
    }
    
    // Getters and Setters
    public List<UserDto> getUsers() {
        return users;
    }
    
    public void setUsers(List<UserDto> users) {
        this.users = users;
    }
    
    public List<String> getCountries() {
        return countries;
    }
    
    public void setCountries(List<String> countries) {
        this.countries = countries;
    }
    

    
    public List<String> getYears() {
        return years;
    }
    
    public void setYears(List<String> years) {
        this.years = years;
    }
    
    public List<String> getContinents() {
        return continents;
    }
    
    public void setContinents(List<String> continents) {
        this.continents = continents;
    }
} 