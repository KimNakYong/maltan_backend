package com.maltan.monitoring.controller;

import com.maltan.monitoring.dto.*;
import com.maltan.monitoring.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    /**
     * 시스템 메트릭 조회
     */
    @GetMapping("/system/metrics")
    public ResponseEntity<ApiResponse<SystemMetrics>> getSystemMetrics() {
        try {
            SystemMetrics metrics = monitoringService.getSystemMetrics();
            return ResponseEntity.ok(ApiResponse.success("시스템 메트릭 조회 성공", metrics));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("시스템 메트릭 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 서비스별 메트릭 조회
     */
    @GetMapping("/services/metrics")
    public ResponseEntity<ApiResponse<List<ServiceMetrics>>> getServicesMetrics() {
        try {
            List<ServiceMetrics> metrics = monitoringService.getServicesMetrics();
            return ResponseEntity.ok(ApiResponse.success("서비스 메트릭 조회 성공", metrics));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("서비스 메트릭 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 데이터베이스 메트릭 조회
     */
    @GetMapping("/databases/metrics")
    public ResponseEntity<ApiResponse<List<DatabaseMetrics>>> getDatabaseMetrics() {
        try {
            List<DatabaseMetrics> metrics = monitoringService.getDatabaseMetrics();
            return ResponseEntity.ok(ApiResponse.success("데이터베이스 메트릭 조회 성공", metrics));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("데이터베이스 메트릭 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 시스템 로그 조회
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<SystemLog>>> getSystemLogs(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String level
    ) {
        try {
            List<SystemLog> logs = monitoringService.getSystemLogs(limit, service, level);
            return ResponseEntity.ok(ApiResponse.success("시스템 로그 조회 성공", logs));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("시스템 로그 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * Health Check
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Monitoring Service is running");
    }
}

