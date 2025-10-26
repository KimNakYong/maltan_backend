package com.maltan.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMetrics {
    private String serviceName;
    private String status;
    private double cpuUsage;
    private double memoryUsage;
    private long memoryLimit;
    private long memoryUsed;
    private String uptime;
}

