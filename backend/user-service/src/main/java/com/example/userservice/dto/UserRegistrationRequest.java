package com.example.userservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UserRegistrationRequest {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    private String password;
    
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자명은 3-50자 사이여야 합니다")
    private String username;
    
    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;
    
    @Valid
    @Size(min = 1, max = 3, message = "선호 지역은 1개 이상 3개 이하여야 합니다")
    private List<PreferredRegionDto> preferredRegions;
    
    // Constructors
    public UserRegistrationRequest() {}
    
    public UserRegistrationRequest(String email, String password, String username, String phone, List<PreferredRegionDto> preferredRegions) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.phone = phone;
        this.preferredRegions = preferredRegions;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
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
}