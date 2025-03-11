package com.huybq.fund_management.exception;

public class DueNotFoundException extends FundException{
    public DueNotFoundException(Integer fundId) {
        super("Không tìm thấy khoản đóng quỹ với ID: " + fundId);
    }
}
