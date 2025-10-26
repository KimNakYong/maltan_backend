package com.maltan.monitoring.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DockerClientBuilder;
import com.maltan.monitoring.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MonitoringService {

    private final DockerClient dockerClient;
    private final OperatingSystemMXBean osBean;
    private final Runtime runtime;

    public MonitoringService() {
        try {
            this.dockerClient = DockerClientBuilder.getInstance().build();
            log.info("Docker client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Docker client", e);
            throw new RuntimeException("Docker client initialization failed", e);
        }
        this.osBean = ManagementFactory.getOperatingSystemMXBean();
        this.runtime = Runtime.getRuntime();
    }

    /**
     * 시스템 메트릭 조회
     */
    public SystemMetrics getSystemMetrics() {
        try {
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;

            // 디스크 정보
            long totalDisk = 0;
            long usedDisk = 0;
            long freeDisk = 0;
            
            try {
                for (FileStore store : FileSystems.getDefault().getFileStores()) {
                    totalDisk += store.getTotalSpace();
                    freeDisk += store.getUsableSpace();
                }
                usedDisk = totalDisk - freeDisk;
            } catch (Exception e) {
                log.warn("Failed to get disk info", e);
            }

            double diskUsage = totalDisk > 0 ? (double) usedDisk / totalDisk * 100 : 0;

            return SystemMetrics.builder()
                    .cpuUsage(osBean.getSystemLoadAverage() * 10) // 대략적인 CPU 사용률
                    .memoryUsage(memoryUsage)
                    .diskUsage(diskUsage)
                    .totalMemory(totalMemory)
                    .usedMemory(usedMemory)
                    .freeMemory(freeMemory)
                    .totalDisk(totalDisk)
                    .usedDisk(usedDisk)
                    .freeDisk(freeDisk)
                    .availableProcessors(runtime.availableProcessors())
                    .systemLoadAverage(osBean.getSystemLoadAverage())
                    .build();
        } catch (Exception e) {
            log.error("Failed to get system metrics", e);
            return SystemMetrics.builder().build();
        }
    }

    /**
     * 서비스별 메트릭 조회 (Docker 컨테이너)
     */
    public List<ServiceMetrics> getServicesMetrics() {
        List<ServiceMetrics> metrics = new ArrayList<>();

        try {
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(false)
                    .exec();

            for (Container container : containers) {
                try {
                    String containerId = container.getId();
                    String serviceName = container.getNames()[0].replace("/", "");

                    InspectContainerResponse inspectResponse = dockerClient.inspectContainerCmd(containerId).exec();
                    InspectContainerResponse.ContainerState state = inspectResponse.getState();

                    // 가동 시간 계산
                    String startedAt = state.getStartedAt();
                    String uptime = calculateUptime(startedAt);

                    // 메모리 및 CPU 통계 (간단한 버전)
                    Statistics stats = dockerClient.statsCmd(containerId)
                            .withNoStream(true)
                            .exec(new com.github.dockerjava.core.InvocationBuilder.AsyncResultCallback<>())
                            .awaitResult();

                    double memoryUsage = 0;
                    long memoryUsed = 0;
                    long memoryLimit = 0;

                    if (stats.getMemoryStats() != null) {
                        memoryUsed = stats.getMemoryStats().getUsage() != null 
                                ? stats.getMemoryStats().getUsage() : 0;
                        memoryLimit = stats.getMemoryStats().getLimit() != null 
                                ? stats.getMemoryStats().getLimit() : 0;
                        memoryUsage = memoryLimit > 0 
                                ? (double) memoryUsed / memoryLimit * 100 : 0;
                    }

                    double cpuUsage = 0;
                    // CPU 사용률 계산은 복잡하므로 간단히 처리
                    if (stats.getCpuStats() != null && stats.getPreCpuStats() != null) {
                        Long cpuDelta = stats.getCpuStats().getCpuUsage().getTotalUsage() - 
                                       stats.getPreCpuStats().getCpuUsage().getTotalUsage();
                        Long systemDelta = stats.getCpuStats().getSystemCpuUsage() - 
                                          stats.getPreCpuStats().getSystemCpuUsage();
                        if (systemDelta > 0 && cpuDelta > 0) {
                            cpuUsage = ((double) cpuDelta / systemDelta) * 
                                      stats.getCpuStats().getOnlineCpus() * 100;
                        }
                    }

                    metrics.add(ServiceMetrics.builder()
                            .serviceName(serviceName)
                            .status(state.getRunning() ? "running" : "stopped")
                            .cpuUsage(Math.min(cpuUsage, 100))
                            .memoryUsage(memoryUsage)
                            .memoryLimit(memoryLimit)
                            .memoryUsed(memoryUsed)
                            .uptime(uptime)
                            .build());

                } catch (Exception e) {
                    log.error("Failed to get metrics for container", e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get services metrics", e);
        }

        return metrics;
    }

    /**
     * 데이터베이스 메트릭 조회 (MySQL, PostgreSQL)
     */
    public List<DatabaseMetrics> getDatabaseMetrics() {
        List<DatabaseMetrics> metrics = new ArrayList<>();

        // MySQL 메트릭
        try {
            DatabaseMetrics mysqlMetrics = getDatabaseMetricsFromCommand("mysql", "3306");
            if (mysqlMetrics != null) {
                metrics.add(mysqlMetrics);
            }
        } catch (Exception e) {
            log.error("Failed to get MySQL metrics", e);
        }

        // PostgreSQL 메트릭
        try {
            DatabaseMetrics postgresMetrics = getDatabaseMetricsFromCommand("postgresql", "5432");
            if (postgresMetrics != null) {
                metrics.add(postgresMetrics);
            }
        } catch (Exception e) {
            log.error("Failed to get PostgreSQL metrics", e);
        }

        return metrics;
    }

    /**
     * 시스템 로그 조회
     */
    public List<SystemLog> getSystemLogs(int limit, String service, String level) {
        List<SystemLog> logs = new ArrayList<>();

        try {
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(false)
                    .exec();

            for (Container container : containers) {
                try {
                    String serviceName = container.getNames()[0].replace("/", "");
                    
                    // 서비스 필터링
                    if (service != null && !service.equals("all") && !serviceName.contains(service.toLowerCase())) {
                        continue;
                    }

                    // 로그 가져오기 (마지막 50줄)
                    final StringBuilder logBuilder = new StringBuilder();
                    com.github.dockerjava.api.async.ResultCallback.Adapter<com.github.dockerjava.api.model.Frame> callback = 
                            new com.github.dockerjava.api.async.ResultCallback.Adapter<>() {
                                @Override
                                public void onNext(com.github.dockerjava.api.model.Frame frame) {
                                    logBuilder.append(new String(frame.getPayload()));
                                }
                            };
                    
                    dockerClient.logContainerCmd(container.getId())
                            .withStdOut(true)
                            .withStdErr(true)
                            .withTail(50)
                            .exec(callback)
                            .awaitCompletion(5, TimeUnit.SECONDS);
                    
                    String logOutput = logBuilder.toString();

                    // 로그 파싱 (간단한 버전)
                    String[] logLines = logOutput.split("\n");
                    for (int i = 0; i < Math.min(logLines.length, 10); i++) {
                        String line = logLines[i];
                        if (line.trim().isEmpty()) continue;

                        String logLevel = detectLogLevel(line);
                        
                        // 레벨 필터링
                        if (level != null && !level.equals("all") && !logLevel.equals(level)) {
                            continue;
                        }

                        logs.add(SystemLog.builder()
                                .id(UUID.randomUUID().toString())
                                .timestamp(LocalDateTime.now())
                                .level(logLevel)
                                .service(serviceName)
                                .message(line.length() > 200 ? line.substring(0, 200) + "..." : line)
                                .userId(null)
                                .build());

                        if (logs.size() >= limit) {
                            break;
                        }
                    }

                } catch (Exception e) {
                    log.error("Failed to get logs for container", e);
                }

                if (logs.size() >= limit) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Failed to get system logs", e);
        }

        return logs;
    }

    // Helper 메서드들

    private String calculateUptime(String startedAt) {
        try {
            Instant start = Instant.parse(startedAt);
            Duration duration = Duration.between(start, Instant.now());
            
            long days = duration.toDays();
            long hours = duration.toHours() % 24;
            long minutes = duration.toMinutes() % 60;
            
            if (days > 0) {
                return String.format("%d일 %d시간", days, hours);
            } else if (hours > 0) {
                return String.format("%d시간 %d분", hours, minutes);
            } else {
                return String.format("%d분", minutes);
            }
        } catch (Exception e) {
            return "알 수 없음";
        }
    }

    private String detectLogLevel(String logLine) {
        String upperLine = logLine.toUpperCase();
        if (upperLine.contains("ERROR") || upperLine.contains("FATAL")) {
            return "ERROR";
        } else if (upperLine.contains("WARN")) {
            return "WARNING";
        } else if (upperLine.contains("DEBUG")) {
            return "DEBUG";
        } else {
            return "INFO";
        }
    }

    private DatabaseMetrics getDatabaseMetricsFromCommand(String type, String port) {
        // 실제 구현에서는 JDBC 연결 또는 시스템 명령어를 사용
        // 여기서는 간단한 mock 데이터 반환
        return DatabaseMetrics.builder()
                .databaseName(type + "db")
                .type(type)
                .status("running")
                .connections(10)
                .maxConnections(100)
                .connectionUsage(10.0)
                .databaseSize(1024 * 1024 * 100) // 100MB
                .tableCount(15)
                .version("8.0" + (type.equals("mysql") ? "" : ".0"))
                .uptime("5시간 30분")
                .build();
    }
}

