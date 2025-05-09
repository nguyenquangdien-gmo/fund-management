package com.huybq.fund_management.domain.adminCreateApi.dto;

import lombok.Data;

@Data
public class PersonalStaffWfhParams {
    private String endDate;
    private String fromDate;
    private Long toDate;
    private String status;
    private Integer page;
    private String userObjId;
}