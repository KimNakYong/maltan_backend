package com.example.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "preferred_regions")
public class PreferredRegion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "도시 코드는 필수입니다")
    @Column(nullable = false)
    private String city;
    
    @NotBlank(message = "도시명은 필수입니다")
    @Column(nullable = false)
    private String cityName;
    
    @NotBlank(message = "구/군 코드는 필수입니다")
    @Column(nullable = false)
    private String district;
    
    @NotBlank(message = "구/군명은 필수입니다")
    @Column(nullable = false)
    private String districtName;
    
    @NotNull(message = "우선순위는 필수입니다")
    @Min(value = 1, message = "우선순위는 1 이상이어야 합니다")
    @Max(value = 3, message = "우선순위는 3 이하여야 합니다")
    @Column(nullable = false)
    private Integer priority;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Constructors
    public PreferredRegion() {}
    
    public PreferredRegion(String city, String cityName, String district, String districtName, Integer priority) {
        this.city = city;
        this.cityName = cityName;
        this.district = district;
        this.districtName = districtName;
        this.priority = priority;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
}
