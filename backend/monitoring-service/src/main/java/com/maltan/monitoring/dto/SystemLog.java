package com.maltan.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {
    private String id;
    private LocalDateTime timestamp;
    private String level;
    private String service;
    private String message;
    private String userId;
}

