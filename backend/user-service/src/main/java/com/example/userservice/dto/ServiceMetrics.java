package com.example.userservice.dto;

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
    private Double cpuUsage;
    private Double memoryUsage;
    private Long memoryLimit;
    private Long memoryUsed;
    private String uptime;
}

