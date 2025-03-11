package com.huybq.fund_management.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends FundException{
    public InvalidAmountException(BigDecimal required, BigDecimal provided) {
        super("Số tiền đóng phải >= " + required + " VND, nhưng chỉ có " + provided + " VND.");
    }
}
