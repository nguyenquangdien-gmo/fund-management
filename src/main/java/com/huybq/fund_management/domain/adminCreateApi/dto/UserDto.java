package com.huybq.fund_management.domain.adminCreateApi.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UserDto {
    private String _id;
    private String userObjId;
    private String name;
    private Integer staffCode;
    private Object positionObjId; // Can be String or Position
    private Object userPositionObjId; // Can be String or Position
    private Object branchObjId; // Can be String or Branch
    private Object departmentObjId; // Can be String or Department
    private String email;
    private String username;
    private String userStatus;
    private String token;
    private String emailPersonal;
    private String userLevel;
    private String userSubPositionObjId;
    private String updatedAt;
    private String uid;
    private String positionCode;
    private String positionName;
    private String positionDescription;
    private String subPositionName;
    private String subPositionCode;
    private String subPositionDescription;
    private String subPositionObjId;
    private String userPositionCode;
    private String userPositionName;
    private String userPositionDescription;
    private String departmentCode;
    private String departmentName;
    private String departmentDescription;
    private String branchCode;
    private String branchName;
    private Boolean isOnsite;
    private Boolean isPM;
    private Boolean isManagerApproveOT;
    private Boolean enableStaffWfh;
    private String birthDay;
    private String phone;
    private String gender;
    private String address;
    private String officialDate;
    private String probationDate;
    private String internDate;
    private String welcomeDate;
    private String timekeepingDate;
    private String quitDate;

    // Needed for dynamic mapping
    private Map<String, Object> additionalProperties;
}
