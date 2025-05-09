package com.huybq.fund_management.domain.adminCreateApi.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String token;
    private int statusCode;
    private String message;
    private T data;
}
