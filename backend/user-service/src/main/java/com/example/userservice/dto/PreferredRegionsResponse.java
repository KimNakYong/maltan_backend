package com.example.userservice.dto;

import java.util.List;

/**
 * 선호 지역 응답 DTO
 */
public class PreferredRegionsResponse {
    
    private List<PreferredRegionDto> preferredRegions;
    
    // Constructors
    public PreferredRegionsResponse() {}
    
    public PreferredRegionsResponse(List<PreferredRegionDto> preferredRegions) {
        this.preferredRegions = preferredRegions;
    }
    
    // Getters and Setters
    public List<PreferredRegionDto> getPreferredRegions() {
        return preferredRegions;
    }
    
    public void setPreferredRegions(List<PreferredRegionDto> preferredRegions) {
        this.preferredRegions = preferredRegions;
    }
}

