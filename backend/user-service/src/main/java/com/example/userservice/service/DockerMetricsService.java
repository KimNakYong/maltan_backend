package com.example.userservice.service;

import com.example.userservice.dto.ServiceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DockerMetricsService {
    
    private static final String[] SERVICES = {
        "user-service",
        "community-service",
        "recommendation-service",
        "place-service",
        "gateway-service"
    };
    
    /**
     * 모든 서비스의 Docker 컨테이너 메트릭 조회
     */
    public List<ServiceMetrics> getAllServiceMetrics() {
        List<ServiceMetrics> metricsList = new ArrayList<>();
        
        for (String serviceName : SERVICES) {
            try {
                ServiceMetrics metrics = getServiceMetrics(serviceName);
                metricsList.add(metrics);
            } catch (Exception e) {
                log.error("Failed to get metrics for service: {}", serviceName, e);
                // 실패한 경우 기본값 추가
                metricsList.add(ServiceMetrics.builder()
                    .serviceName(serviceName)
                    .status("unknown")
                    .cpuUsage(0.0)
                    .memoryUsage(0.0)
                    .memoryLimit(0L)
                    .memoryUsed(0L)
                    .uptime("N/A")
                    .build());
            }
        }
        
        return metricsList;
    }
    
    /**
     * 특정 서비스의 Docker 컨테이너 메트릭 조회
     */
    private ServiceMetrics getServiceMetrics(String serviceName) throws Exception {
        // Docker stats 명령어 실행
        ProcessBuilder pb = new ProcessBuilder(
            "docker", "stats", serviceName, 
            "--no-stream", 
            "--format", "{{.CPUPerc}},{{.MemPerc}},{{.MemUsage}}"
        );
        pb.redirectErrorStream(true); // 에러 스트림을 표준 출력으로 병합
        
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        String line = reader.readLine();
        int exitCode = process.waitFor();
        
        // 에러 로그 출력
        if (exitCode != 0 || line == null || line.isEmpty()) {
            StringBuilder errorOutput = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorOutput.append(errorLine).append("\n");
            }
            log.error("Docker stats failed for {}: exit code {}, output: {}, error: {}", 
                serviceName, exitCode, line, errorOutput.toString());
            throw new RuntimeException("No stats available for " + serviceName);
        }
        
        log.debug("Docker stats for {}: {}", serviceName, line);
        
        // 결과 파싱: "5.23%,12.45%,256MiB / 2GiB"
        String[] parts = line.split(",");
        
        if (parts.length < 3) {
            log.error("Invalid docker stats format for {}: {}", serviceName, line);
            throw new RuntimeException("Invalid stats format for " + serviceName);
        }
        
        double cpuUsage = parseCpuUsage(parts[0]);
        double memoryUsage = parseMemoryUsage(parts[1]);
        long[] memoryInfo = parseMemoryInfo(parts[2]);
        
        // 컨테이너 상태 확인
        String status = getContainerStatus(serviceName);
        String uptime = getContainerUptime(serviceName);
        
        return ServiceMetrics.builder()
            .serviceName(serviceName)
            .status(status)
            .cpuUsage(cpuUsage)
            .memoryUsage(memoryUsage)
            .memoryUsed(memoryInfo[0])
            .memoryLimit(memoryInfo[1])
            .uptime(uptime)
            .build();
    }
    
    private double parseCpuUsage(String cpuStr) {
        try {
            return Double.parseDouble(cpuStr.replace("%", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private double parseMemoryUsage(String memStr) {
        try {
            return Double.parseDouble(memStr.replace("%", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private long[] parseMemoryInfo(String memUsageStr) {
        try {
            // "256MiB / 2GiB" 형식 파싱
            String[] parts = memUsageStr.split("/");
            long used = parseMemorySize(parts[0].trim());
            long limit = parseMemorySize(parts[1].trim());
            return new long[]{used, limit};
        } catch (Exception e) {
            return new long[]{0L, 0L};
        }
    }
    
    private long parseMemorySize(String sizeStr) {
        try {
            sizeStr = sizeStr.trim();
            if (sizeStr.endsWith("GiB")) {
                return (long) (Double.parseDouble(sizeStr.replace("GiB", "")) * 1024 * 1024 * 1024);
            } else if (sizeStr.endsWith("MiB")) {
                return (long) (Double.parseDouble(sizeStr.replace("MiB", "")) * 1024 * 1024);
            } else if (sizeStr.endsWith("KiB")) {
                return (long) (Double.parseDouble(sizeStr.replace("KiB", "")) * 1024);
            }
            return 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
    
    private String getContainerStatus(String serviceName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "inspect", serviceName, 
                "--format", "{{.State.Status}}"
            );
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String status = reader.readLine();
            process.waitFor();
            return status != null ? status.trim() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private String getContainerUptime(String serviceName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "inspect", serviceName, 
                "--format", "{{.State.StartedAt}}"
            );
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String startedAt = reader.readLine();
            process.waitFor();
            
            if (startedAt != null && !startedAt.isEmpty()) {
                // 간단한 uptime 계산 (실제로는 더 정교한 계산 필요)
                return "Running";
            }
            return "N/A";
        } catch (Exception e) {
            return "N/A";
        }
    }
}

