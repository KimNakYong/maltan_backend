package com.example.userservice.service;

import com.example.userservice.dto.SystemMetrics;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

@Service
public class SystemMetricsService {

    public SystemMetrics getSystemMetrics() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();
        
        // CPU 사용률 계산
        double systemLoadAverage = osBean.getSystemLoadAverage();
        int availableProcessors = osBean.getAvailableProcessors();
        double cpuUsage = systemLoadAverage >= 0 
            ? (systemLoadAverage / availableProcessors) * 100 
            : 0; // -1이면 사용 불가
        
        // 메모리 사용률 계산
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (usedMemory / (double) totalMemory) * 100;
        
        // 디스크 사용률 계산
        File root = new File("/");
        long totalDisk = root.getTotalSpace();
        long freeDisk = root.getFreeSpace();
        long usedDisk = totalDisk - freeDisk;
        double diskUsage = totalDisk > 0 ? (usedDisk / (double) totalDisk) * 100 : 0;
        
        return SystemMetrics.builder()
                .cpuUsage(Math.min(cpuUsage, 100)) // 최대 100%로 제한
                .memoryUsage(memoryUsage)
                .diskUsage(diskUsage)
                .totalMemory(totalMemory)
                .usedMemory(usedMemory)
                .freeMemory(freeMemory)
                .totalDisk(totalDisk)
                .usedDisk(usedDisk)
                .freeDisk(freeDisk)
                .availableProcessors(availableProcessors)
                .systemLoadAverage(systemLoadAverage)
                .build();
    }
}

