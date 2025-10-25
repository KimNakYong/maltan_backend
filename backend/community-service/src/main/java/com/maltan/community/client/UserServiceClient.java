package com.maltan.community.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${user.service.url:http://localhost:8081}")
    private String userServiceUrl;
    
    /**
     * User Service에서 사용자 이름 조회
     */
    public String getUserName(Long userId) {
        try {
            String url = userServiceUrl + "/api/user/internal/users/" + userId + "/name";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("name")) {
                return (String) response.get("name");
            }
            
            log.warn("User Service에서 사용자 이름을 찾을 수 없음: userId={}", userId);
            return "사용자" + userId;
        } catch (Exception e) {
            log.error("User Service 호출 실패: userId={}", userId, e);
            return "사용자" + userId;
        }
    }
    
    /**
     * 여러 사용자의 이름을 한 번에 조회 (배치 처리)
     */
    public Map<Long, String> getUserNames(Iterable<Long> userIds) {
        Map<Long, String> userNames = new HashMap<>();
        
        for (Long userId : userIds) {
            userNames.put(userId, getUserName(userId));
        }
        
        return userNames;
    }
}

