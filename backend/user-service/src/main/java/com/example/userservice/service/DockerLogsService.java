package com.example.userservice.service;

import com.example.userservice.dto.SystemLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DockerLogsService {
    
    private static final String[] SERVICES = {
        "user-service",
        "community-service",
        "recommendation-service",
        "place-service",
        "gateway-service"
    };
    
    private static final Pattern LOG_LEVEL_PATTERN = Pattern.compile("(ERROR|WARN|INFO|DEBUG|TRACE)", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 모든 서비스의 Docker 로그 조회
     */
    public List<SystemLog> getAllServiceLogs(int limit) {
        List<SystemLog> allLogs = new ArrayList<>();
        
        for (String serviceName : SERVICES) {
            try {
                List<SystemLog> serviceLogs = getServiceLogs(serviceName, limit / SERVICES.length);
                allLogs.addAll(serviceLogs);
            } catch (Exception e) {
                log.error("Failed to get logs for service: {}", serviceName, e);
            }
        }
        
        // 시간순 정렬 (최신순)
        allLogs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        // limit 적용
        if (allLogs.size() > limit) {
            return allLogs.subList(0, limit);
        }
        
        return allLogs;
    }
    
    /**
     * 특정 서비스의 Docker 로그 조회
     */
    public List<SystemLog> getServiceLogs(String serviceName, int lines) {
        List<SystemLog> logs = new ArrayList<>();
        
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "logs", serviceName, 
                "--tail", String.valueOf(lines),
                "--timestamps"
            );
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                SystemLog log = parseLogLine(line, serviceName);
                if (log != null) {
                    logs.add(log);
                }
            }
            
            process.waitFor();
            
        } catch (Exception e) {
            log.error("Failed to get Docker logs for {}", serviceName, e);
        }
        
        return logs;
    }
    
    /**
     * 로그 라인 파싱
     */
    private SystemLog parseLogLine(String line, String serviceName) {
        try {
            // Docker 타임스탬프 추출 (예: 2025-10-25T12:34:56.789Z)
            String timestamp = extractTimestamp(line);
            String level = extractLogLevel(line);
            String message = extractMessage(line);
            String userId = extractUserId(line);
            
            return SystemLog.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(timestamp)
                .level(level)
                .service(formatServiceName(serviceName))
                .message(message)
                .userId(userId)
                .build();
                
        } catch (Exception e) {
            log.debug("Failed to parse log line: {}", line, e);
            return null;
        }
    }
    
    private String extractTimestamp(String line) {
        // Docker 타임스탬프 형식: 2025-10-25T12:34:56.789012345Z
        Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})");
        Matcher matcher = pattern.matcher(line);
        
        if (matcher.find()) {
            String timestamp = matcher.group(1);
            // ISO 형식을 로컬 형식으로 변환
            return timestamp.replace("T", " ");
        }
        
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
    
    private String extractLogLevel(String line) {
        Matcher matcher = LOG_LEVEL_PATTERN.matcher(line);
        if (matcher.find()) {
            String level = matcher.group(1).toUpperCase();
            // WARN을 WARNING으로 변환
            return level.equals("WARN") ? "WARNING" : level;
        }
        return "INFO";
    }
    
    private String extractMessage(String line) {
        // 타임스탬프 제거
        String message = line.replaceFirst("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z\\s*", "");
        
        // 너무 긴 메시지는 자르기
        if (message.length() > 500) {
            message = message.substring(0, 497) + "...";
        }
        
        return message.trim();
    }
    
    private String extractUserId(String line) {
        // userId= 또는 user_id= 패턴 찾기
        Pattern pattern = Pattern.compile("user[_-]?id[=:]\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    private String formatServiceName(String serviceName) {
        // "user-service" -> "User"
        String name = serviceName.replace("-service", "");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}

