package com.example.userservice.service;

import com.example.userservice.dto.DatabaseMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DatabaseMetricsService {
    
    @Value("${spring.datasource.url}")
    private String mysqlUrl;
    
    @Value("${spring.datasource.username}")
    private String mysqlUsername;
    
    @Value("${spring.datasource.password}")
    private String mysqlPassword;
    
    /**
     * 모든 데이터베이스 메트릭 조회
     */
    public List<DatabaseMetrics> getAllDatabaseMetrics() {
        List<DatabaseMetrics> metricsList = new ArrayList<>();
        
        // MySQL 메트릭
        try {
            DatabaseMetrics mysqlMetrics = getMySQLMetrics();
            metricsList.add(mysqlMetrics);
        } catch (Exception e) {
            log.error("Failed to get MySQL metrics", e);
            metricsList.add(createErrorMetrics("MySQL", "mysql"));
        }
        
        // PostgreSQL 메트릭
        try {
            DatabaseMetrics postgresMetrics = getPostgreSQLMetrics();
            metricsList.add(postgresMetrics);
        } catch (Exception e) {
            log.error("Failed to get PostgreSQL metrics", e);
            metricsList.add(createErrorMetrics("PostgreSQL", "postgresql"));
        }
        
        // Redis 메트릭
        try {
            DatabaseMetrics redisMetrics = getRedisMetrics();
            metricsList.add(redisMetrics);
        } catch (Exception e) {
            log.error("Failed to get Redis metrics", e);
            metricsList.add(createErrorMetrics("Redis", "redis"));
        }
        
        return metricsList;
    }
    
    /**
     * MySQL 메트릭 조회
     */
    private DatabaseMetrics getMySQLMetrics() throws Exception {
        try (Connection conn = DriverManager.getConnection(mysqlUrl, mysqlUsername, mysqlPassword);
             Statement stmt = conn.createStatement()) {
            
            // 버전 조회
            String version = "Unknown";
            try (ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
                if (rs.next()) {
                    version = rs.getString(1);
                }
            }
            
            // 연결 수 조회
            long connections = 0;
            long maxConnections = 0;
            try (ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_connected'")) {
                if (rs.next()) {
                    connections = rs.getLong(2);
                }
            }
            try (ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE 'max_connections'")) {
                if (rs.next()) {
                    maxConnections = rs.getLong(2);
                }
            }
            
            // 데이터베이스 크기 조회
            long databaseSize = 0;
            try (ResultSet rs = stmt.executeQuery(
                "SELECT SUM(data_length + index_length) as size " +
                "FROM information_schema.TABLES " +
                "WHERE table_schema = DATABASE()")) {
                if (rs.next()) {
                    databaseSize = rs.getLong("size");
                }
            }
            
            // 테이블 수 조회
            long tableCount = 0;
            try (ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) as count FROM information_schema.TABLES WHERE table_schema = DATABASE()")) {
                if (rs.next()) {
                    tableCount = rs.getLong("count");
                }
            }
            
            // Uptime 조회
            String uptime = "Unknown";
            try (ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Uptime'")) {
                if (rs.next()) {
                    long uptimeSeconds = rs.getLong(2);
                    uptime = formatUptime(uptimeSeconds);
                }
            }
            
            double connectionUsage = maxConnections > 0 ? (connections * 100.0 / maxConnections) : 0;
            
            return DatabaseMetrics.builder()
                .databaseName("MySQL (User Service)")
                .type("mysql")
                .status("running")
                .connections(connections)
                .maxConnections(maxConnections)
                .connectionUsage(connectionUsage)
                .databaseSize(databaseSize)
                .tableCount(tableCount)
                .version(version)
                .uptime(uptime)
                .build();
        }
    }
    
    /**
     * PostgreSQL 메트릭 조회
     */
    private DatabaseMetrics getPostgreSQLMetrics() throws Exception {
        String postgresUrl = "jdbc:postgresql://10.0.2.15:5432/community_db";
        String postgresUsername = "community_user";
        String postgresPassword = "Community@2025!";
        
        try (Connection conn = DriverManager.getConnection(postgresUrl, postgresUsername, postgresPassword);
             Statement stmt = conn.createStatement()) {
            
            // 버전 조회
            String version = "Unknown";
            try (ResultSet rs = stmt.executeQuery("SELECT version()")) {
                if (rs.next()) {
                    String fullVersion = rs.getString(1);
                    // "PostgreSQL 15.3 ..." -> "15.3"
                    version = fullVersion.split(" ")[1];
                }
            }
            
            // 연결 수 조회
            long connections = 0;
            long maxConnections = 0;
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM pg_stat_activity")) {
                if (rs.next()) {
                    connections = rs.getLong(1);
                }
            }
            try (ResultSet rs = stmt.executeQuery("SHOW max_connections")) {
                if (rs.next()) {
                    maxConnections = rs.getLong(1);
                }
            }
            
            // 데이터베이스 크기 조회
            long databaseSize = 0;
            try (ResultSet rs = stmt.executeQuery("SELECT pg_database_size(current_database())")) {
                if (rs.next()) {
                    databaseSize = rs.getLong(1);
                }
            }
            
            // 테이블 수 조회
            long tableCount = 0;
            try (ResultSet rs = stmt.executeQuery(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'")) {
                if (rs.next()) {
                    tableCount = rs.getLong(1);
                }
            }
            
            // Uptime 조회
            String uptime = "Unknown";
            try (ResultSet rs = stmt.executeQuery(
                "SELECT EXTRACT(EPOCH FROM (now() - pg_postmaster_start_time()))::bigint")) {
                if (rs.next()) {
                    long uptimeSeconds = rs.getLong(1);
                    uptime = formatUptime(uptimeSeconds);
                }
            }
            
            double connectionUsage = maxConnections > 0 ? (connections * 100.0 / maxConnections) : 0;
            
            return DatabaseMetrics.builder()
                .databaseName("PostgreSQL (Community/Place/Recommendation)")
                .type("postgresql")
                .status("running")
                .connections(connections)
                .maxConnections(maxConnections)
                .connectionUsage(connectionUsage)
                .databaseSize(databaseSize)
                .tableCount(tableCount)
                .version(version)
                .uptime(uptime)
                .build();
        }
    }
    
    /**
     * Redis 메트릭 조회 (Docker stats 사용)
     */
    private DatabaseMetrics getRedisMetrics() throws Exception {
        // Redis는 Docker 컨테이너로 실행 중이므로 docker stats로 조회
        ProcessBuilder pb = new ProcessBuilder(
            "docker", "exec", "redis", "redis-cli", "INFO"
        );
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(process.getInputStream()));
        
        String version = "Unknown";
        String uptime = "Unknown";
        long connections = 0;
        long maxConnections = 10000; // Redis 기본값
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("redis_version:")) {
                version = line.split(":")[1].trim();
            } else if (line.startsWith("connected_clients:")) {
                connections = Long.parseLong(line.split(":")[1].trim());
            } else if (line.startsWith("uptime_in_seconds:")) {
                long uptimeSeconds = Long.parseLong(line.split(":")[1].trim());
                uptime = formatUptime(uptimeSeconds);
            } else if (line.startsWith("maxclients:")) {
                maxConnections = Long.parseLong(line.split(":")[1].trim());
            }
        }
        
        process.waitFor();
        
        double connectionUsage = maxConnections > 0 ? (connections * 100.0 / maxConnections) : 0;
        
        return DatabaseMetrics.builder()
            .databaseName("Redis (Cache)")
            .type("redis")
            .status("running")
            .connections(connections)
            .maxConnections(maxConnections)
            .connectionUsage(connectionUsage)
            .databaseSize(0L) // Redis는 메모리 DB이므로 크기 계산 생략
            .tableCount(0L)
            .version(version)
            .uptime(uptime)
            .build();
    }
    
    private DatabaseMetrics createErrorMetrics(String name, String type) {
        return DatabaseMetrics.builder()
            .databaseName(name)
            .type(type)
            .status("error")
            .connections(0L)
            .maxConnections(0L)
            .connectionUsage(0.0)
            .databaseSize(0L)
            .tableCount(0L)
            .version("Unknown")
            .uptime("Unknown")
            .build();
    }
    
    private String formatUptime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        
        if (days > 0) {
            return String.format("%d일 %d시간", days, hours);
        } else if (hours > 0) {
            return String.format("%d시간 %d분", hours, minutes);
        } else {
            return String.format("%d분", minutes);
        }
    }
}

