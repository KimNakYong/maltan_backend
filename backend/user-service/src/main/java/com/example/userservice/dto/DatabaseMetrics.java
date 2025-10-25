package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseMetrics {
    private String databaseName;
    private String type; // MySQL, PostgreSQL, Redis
    private String status;
    private Long connections;
    private Long maxConnections;
    private Double connectionUsage; // 연결 사용률 (%)
    private Long databaseSize; // 데이터베이스 크기 (bytes)
    private Long tableCount; // 테이블 수
    private String version;
    private String uptime;
}

