package com.example.userservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PreferredRegionDto {
    
    @NotBlank(message = "도시 코드는 필수입니다")
    private String city;
    
    @NotBlank(message = "도시명은 필수입니다")
    private String cityName;
    
    @NotBlank(message = "구/군 코드는 필수입니다")
    private String district;
    
    @NotBlank(message = "구/군명은 필수입니다")
    private String districtName;
    
    @NotNull(message = "우선순위는 필수입니다")
    @Min(value = 1, message = "우선순위는 1 이상이어야 합니다")
    @Max(value = 3, message = "우선순위는 3 이하여야 합니다")
    private Integer priority;
    
    // Constructors
    public PreferredRegionDto() {}
    
    public PreferredRegionDto(String city, String cityName, String district, String districtName, Integer priority) {
        this.city = city;
        this.cityName = cityName;
        this.district = district;
        this.districtName = districtName;
        this.priority = priority;
    }
    
    // Getters and Setters
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getCityName() {
        return cityName;
    }
    
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getDistrictName() {
        return districtName;
    }
    
    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
