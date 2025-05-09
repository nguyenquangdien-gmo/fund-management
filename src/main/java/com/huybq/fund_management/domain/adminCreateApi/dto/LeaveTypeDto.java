package com.huybq.fund_management.domain.adminCreateApi.dto;

import lombok.Data;

import java.util.Map;

@Data
public class LeaveTypeDto {
    private String _id;
    private String attendanceTypeName;
    private String attendanceTypeCode;
    private Double attendanceWorkDay;
    private String type;
    private String status;

    // Needed for dynamic mapping
    private Map<String, Object> additionalProperties;
}