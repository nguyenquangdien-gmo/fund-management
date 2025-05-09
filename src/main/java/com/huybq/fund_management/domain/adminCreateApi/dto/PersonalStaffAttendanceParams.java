package com.huybq.fund_management.domain.adminCreateApi.dto;

import lombok.Data;

@Data
public class PersonalStaffAttendanceParams {
    private String startDate;
    private String endDate;
    private Long fromDate;
    private Long toDate;
    private Integer page;
    private String All;
    private String userObjId;
}