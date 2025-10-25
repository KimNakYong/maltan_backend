package com.example.userservice.dto;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 프로필 업데이트 요청 DTO
 */
public class UpdateProfileRequest {
    
    private String name;
    private String phoneNumber;
    
    @Valid
    private List<PreferredRegionDto> preferredRegions;
    
    // Constructors
    public UpdateProfileRequest() {}
    
    public UpdateProfileRequest(String name, String phoneNumber, List<PreferredRegionDto> preferredRegions) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.preferredRegions = preferredRegions;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public List<PreferredRegionDto> getPreferredRegions() {
        return preferredRegions;
    }
    
    public void setPreferredRegions(List<PreferredRegionDto> preferredRegions) {
        this.preferredRegions = preferredRegions;
    }
}

