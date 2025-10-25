package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {
    private String id;
    private String timestamp;
    private String level;
    private String service;
    private String message;
    private String userId;
}

