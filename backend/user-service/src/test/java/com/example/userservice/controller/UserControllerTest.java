package com.example.userservice.controller;

import com.example.userservice.dto.PreferredRegionDto;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
public class UserControllerTest {
    
    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @Test
    public void testRegisterUser() throws Exception {
        setup();
        
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setUsername("testuser");
        request.setPhone("010-1234-5678");
        
        // 선호 지역 추가
        List<PreferredRegionDto> regions = List.of(
            new PreferredRegionDto("seoul", "서울특별시", "gangnam", "강남구", 1)
        );
        request.setPreferredRegions(regions);
        
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }
    
    @Test
    public void testCheckUsernameExists() throws Exception {
        setup();
        
        // 테스트용 사용자 생성
        User user = new User();
        user.setUsername("existinguser");
        user.setEmail("existing@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Existing User");
        userRepository.save(user);
        
        mockMvc.perform(get("/api/users/check-username/existinguser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
        
        mockMvc.perform(get("/api/users/check-username/nonexistinguser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(false));
    }
    
    @Test
    public void testHealthCheck() throws Exception {
        setup();
        
        mockMvc.perform(get("/api/users/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("user-service"));
    }
}