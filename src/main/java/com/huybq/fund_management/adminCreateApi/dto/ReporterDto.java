package com.huybq.fund_management.adminCreateApi.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ReporterDto {
    private String _id;
    private String name;
    private String username;
    private String email;
    private Integer staffCode;
    private Object userPositionObjId; // Can be String or Position
    private Object departmentObjId; // Can be String or Department
    private Object positionObjId; // Can be String or Position
    private Object branchObjId; // Can be String or Branch
    private String userLevel;
    private String status;
    private UserInfoDto UserInfo;
    private String id;

    // Needed for dynamic mapping
    private Map<String, Object> additionalProperties;
}
