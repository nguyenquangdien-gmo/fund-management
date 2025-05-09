package com.huybq.fund_management.domain.adminCreateApi.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateLeaveRequestDto {
    private String userObjId;
    private String attendanceTypeObjId;
    private String reportObjId;
    private String fromDate;
    private String endDate;
    private String reason;
    private List<String> relationshipObjIds;
}
