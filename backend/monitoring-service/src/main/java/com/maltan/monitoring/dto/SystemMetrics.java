package com.maltan.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetrics {
    private double cpuUsage;
    private double memoryUsage;
    private double diskUsage;
    private long totalMemory;
    private long usedMemory;
    private long freeMemory;
    private long totalDisk;
    private long usedDisk;
    private long freeDisk;
    private int availableProcessors;
    private double systemLoadAverage;
}

