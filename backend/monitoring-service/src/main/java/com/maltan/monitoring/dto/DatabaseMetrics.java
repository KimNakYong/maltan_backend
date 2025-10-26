package com.maltan.monitoring.dto;

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
    private String type; // mysql, postgresql, redis
    private String status;
    private int connections;
    private int maxConnections;
    private double connectionUsage;
    private long databaseSize;
    private int tableCount;
    private String version;
    private String uptime;
}

