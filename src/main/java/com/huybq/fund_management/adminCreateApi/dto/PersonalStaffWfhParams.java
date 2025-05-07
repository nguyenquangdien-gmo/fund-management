package com.huybq.fund_management.adminCreateApi.dto;

import lombok.Data;

@Data
public class PersonalStaffWfhParams {
    private String startDate;
    private String endDate;
    private String fromDate;
    private Integer toDate;
    private Integer page;
    private String userObjId;
}
