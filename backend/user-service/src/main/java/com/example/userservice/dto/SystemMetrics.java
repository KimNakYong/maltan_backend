package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetrics {
    private double cpuUsage;           // CPU 사용률 (%)
    private double memoryUsage;        // 메모리 사용률 (%)
    private double diskUsage;          // 디스크 사용률 (%)
    private long totalMemory;          // 전체 메모리 (bytes)
    private long usedMemory;           // 사용 중인 메모리 (bytes)
    private long freeMemory;           // 여유 메모리 (bytes)
    private long totalDisk;            // 전체 디스크 (bytes)
    private long usedDisk;             // 사용 중인 디스크 (bytes)
    private long freeDisk;             // 여유 디스크 (bytes)
    private int availableProcessors;   // 사용 가능한 프로세서 수
    private double systemLoadAverage;  // 시스템 부하 평균
}

