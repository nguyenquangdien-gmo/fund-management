package com.huybq.fund_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FundManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FundManagementApplication.class, args);
    }

}
