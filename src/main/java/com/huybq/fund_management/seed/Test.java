package com.huybq.fund_management.seed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            // Cắt bỏ phần "(Indochina Time)"
            dateStr = dateStr.replaceAll("\\s*\\(.*\\)", "");

            // Định dạng chuỗi ngày giờ với pattern "EEE MMM dd yyyy HH:mm:ss 'GMT'Z"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);
            Date date = Date.from(DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH)
                    .parse(dateStr, ZonedDateTime::from).toInstant());

            // Chuyển đổi thành LocalDate
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (Exception e) {
            throw new IllegalArgumentException( e);
        }
    }


    public static void main(String[] args) {
        String dateStr = "Thu Jun 15 1995 07:00:00 GMT+0700 (Indochina Time)";
        LocalDate result = parseDate(dateStr);
        System.out.println(result); // In ra kết quả LocalDateTime
    }



}
