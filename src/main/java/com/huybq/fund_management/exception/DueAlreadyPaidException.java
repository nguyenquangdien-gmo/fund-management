package com.huybq.fund_management.exception;

public class DueAlreadyPaidException extends FundException{
    public DueAlreadyPaidException(Integer fundId) {
        super("Khoản đóng quỹ với ID: " + fundId + " đã được thanh toán");
    }
}
