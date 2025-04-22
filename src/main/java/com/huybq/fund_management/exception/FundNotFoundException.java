package com.huybq.fund_management.exception;

public class FundNotFoundException extends FundException{
    public FundNotFoundException(Integer fundId) {
        super("Không tìm thấy quỹ với ID: " + fundId);
    }
}
