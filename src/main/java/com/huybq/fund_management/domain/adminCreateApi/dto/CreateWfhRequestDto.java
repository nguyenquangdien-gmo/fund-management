package com.huybq.fund_management.domain.adminCreateApi.dto;

import lombok.Data;

@Data
public class CreateWfhRequestDto {
    private String applicant_person;
    private Integer staff_code;
    private String positon;
    private String department;
    private String reportObjId;
    private String createAt;
    private String fromDate;
    private String endDate;
    private String reason;
    private String userObjId;
}
