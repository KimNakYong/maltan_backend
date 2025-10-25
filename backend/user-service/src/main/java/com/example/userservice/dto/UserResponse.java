package com.example.userservice.dto;

import com.example.userservice.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserResponse {
    
    private Long id;
    private String username;
    private String email;
    private String name;
    private String phone;
    private List<PreferredRegionDto> preferredRegions;
    private User.Role role;
    private Boolean isEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserResponse() {}
    
    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.name = user.getName();
        this.phone = user.getPhoneNumber();
        
        // JSON에서 선호지역 파싱
        this.preferredRegions = parsePreferredRegionsFromJson(user.getPreferredRegionsJson());
        
        this.role = user.getRole();
        this.isEnabled = user.isEnabled();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
    
    private List<PreferredRegionDto> parsePreferredRegionsFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(json);
            List<PreferredRegionDto> regions = new ArrayList<>();
            
            if (jsonNode.isArray()) {
                for (JsonNode regionNode : jsonNode) {
                    PreferredRegionDto region = new PreferredRegionDto(
                        regionNode.get("city").asText(),
                        regionNode.get("cityName").asText(),
                        regionNode.get("district").asText(),
                        regionNode.get("districtName").asText(),
                        regionNode.get("priority").asInt()
                    );
                    regions.add(region);
                }
            }
            return regions;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public List<PreferredRegionDto> getPreferredRegions() {
        return preferredRegions;
    }
    
    public void setPreferredRegions(List<PreferredRegionDto> preferredRegions) {
        this.preferredRegions = preferredRegions;
    }
    
    public User.Role getRole() {
        return role;
    }
    
    public void setRole(User.Role role) {
        this.role = role;
    }
    
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
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
}