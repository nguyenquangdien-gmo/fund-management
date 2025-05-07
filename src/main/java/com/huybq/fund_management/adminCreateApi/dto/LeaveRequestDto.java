package com.huybq.fund_management.adminCreateApi.dto;

import lombok.Data;

import java.util.Map;

@Data
public class LeaveRequestDto {
    private String _id;
    private String fromDate;
    private String endDate;
    private String reason;
    private String statusApproval;
    private Object userObjId; // Can be String or UserDto
    private Object reportObjId; // Can be String or ReporterDto
    private Object attendanceTypeObjId; // Can be String or AttendanceTypeDto
    private Double absentDay;
    private String createdAt;
    private String type;

    // Needed for dynamic mapping
    private Map<String, Object> additionalProperties;
}
