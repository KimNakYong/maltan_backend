package com.example.userservice.service;

import com.example.userservice.dto.ChangePasswordRequest;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.PreferredRegionDto;
import com.example.userservice.dto.PreferredRegionsResponse;
import com.example.userservice.dto.UpdateProfileRequest;
import com.example.userservice.dto.UserRegistrationRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 사용자 등록
     */
    public UserResponse registerUser(UserRegistrationRequest request) {
        // username이 없으면 email을 사용
        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = request.getEmail();
        }
        
        // 중복 검사
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("이미 존재하는 사용자명입니다: " + username);
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        
        // 새 사용자 생성
        User user = new User();
        user.setUsername(username);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName()); // 실제 이름 사용
        user.setPhoneNumber(request.getPhone());
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        
    // 선호 지역을 JSON으로 변환하여 저장
    if (request.getPreferredRegions() != null && !request.getPreferredRegions().isEmpty()) {
        String preferredRegionsJson = convertPreferredRegionsToJson(request.getPreferredRegions());
        user.setPreferredRegionsJson(preferredRegionsJson);
    }
        
        // 사용자 저장
        User savedUser = userRepository.save(user);
        
        return new UserResponse(savedUser);
    }
    
    /**
     * 사용자명으로 사용자 찾기
     */
    public Optional<UserResponse> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserResponse::new);
    }
    
    /**
     * 이메일로 사용자 찾기
     */
    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponse::new);
    }
    
    /**
     * 사용자 ID로 사용자 찾기
     */
    public Optional<UserResponse> findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::new);
    }
    
    /**
     * 사용자명 또는 이메일로 사용자 찾기
     */
    public Optional<UserResponse> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail)
                .map(UserResponse::new);
    }
    
    /**
     * 모든 사용자 조회
     */
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::new)
                .toList();
    }
    
    /**
     * 사용자명 존재 여부 확인
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * 이메일 존재 여부 확인
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * 사용자 활성화/비활성화
     */
    public UserResponse updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);
        
        return new UserResponse(updatedUser);
    }
    
    /**
     * 사용자 정보 업데이트
     */
    public UserResponse updateUser(Long userId, UserRegistrationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        // 이메일 중복 검사 (자신의 이메일이 아닌 경우)
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        
        user.setEmail(request.getEmail());
        user.setName(request.getUsername());
        user.setPhoneNumber(request.getPhone());
        
        // 비밀번호가 제공된 경우에만 업데이트
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser);
    }
    
    /**
     * 사용자 삭제
     */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + userId);
        }
        userRepository.deleteById(userId);
    }
    
    /**
     * UserDetailsService 구현
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        
        return user;
    }
    
    /**
     * 로그인 (이메일/비밀번호 검증)
     */
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 일치하지 않습니다"));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 일치하지 않습니다");
        }
        
        // 계정 활성화 확인
        if (!user.isEnabled()) {
            throw new RuntimeException("비활성화된 계정입니다");
        }
        
        return new UserResponse(user);
    }
    
    /**
     * 현재 사용자의 선호 지역 조회 (이메일 기반)
     */
    public PreferredRegionsResponse getMyPreferredRegions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
        
        UserResponse userResponse = new UserResponse(user);
        return new PreferredRegionsResponse(userResponse.getPreferredRegions());
    }
    
    /**
     * 현재 사용자 프로필 업데이트
     */
    public UserResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
        
        // 이름 업데이트
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName());
        }
        
        // 전화번호 업데이트
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        
        // 선호 지역 업데이트
        if (request.getPreferredRegions() != null && !request.getPreferredRegions().isEmpty()) {
            String preferredRegionsJson = convertPreferredRegionsToJson(request.getPreferredRegions());
            user.setPreferredRegionsJson(preferredRegionsJson);
        }
        
        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser);
    }
    
    /**
     * 현재 사용자 비밀번호 변경
     */
    public void changeMyPassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다");
        }
        
        // 새 비밀번호로 변경
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    /**
     * 선호지역 리스트를 JSON 문자열로 변환
     */
    private String convertPreferredRegionsToJson(List<PreferredRegionDto> preferredRegions) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(preferredRegions);
        } catch (Exception e) {
            throw new RuntimeException("선호지역 JSON 변환 실패", e);
        }
    }
}