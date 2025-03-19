package com.huybq.fund_management.seed;

import com.huybq.fund_management.domain.balance.Balance;
import com.huybq.fund_management.domain.balance.BalanceRepository;
import com.huybq.fund_management.domain.contributions.ContributionRepository;
import com.huybq.fund_management.domain.fund.Fund;
import com.huybq.fund_management.domain.fund.FundRepository;
import com.huybq.fund_management.domain.fund.FundType;
import com.huybq.fund_management.domain.period.PeriodRepository;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataLoader {
    @Bean
    public CommandLineRunner init(ContributionRepository repository) {
        return args -> {
            // Tạo dữ liệu cho Balance
            Balance common = new Balance();
            common.setTitle("common_fund");
            common.setTotalAmount(new BigDecimal(0));
        };
    }

//    @Bean
//    public CommandLineRunner initData(BalanceRepository balanceRepository, FundRepository fundRepository) {
//        return args -> {
//            // Tạo dữ liệu cho Balance
//            Balance common = new Balance();
//            common.setTitle("common_fund");
//            common.setTotalAmount(new BigDecimal(0));
//
//            Balance snack = new Balance();
//            snack.setTitle("snack_fund");
//            snack.setTotalAmount(new BigDecimal(0));
//
//
//
//            balanceRepository.saveAll(Arrays.asList(common, snack));
//
//            // Tạo dữ liệu cho Fund
//            Fund fund1 = new Fund();
//            fund1.setName("snack_fund");
//            fund1.setDescription("Quỹ dành cho ăn vặt hàng tháng");
//            fund1.setType(FundType.SNACK);
//            fund1.setAmount(new BigDecimal(120000));
//
//            Fund fund2 = new Fund();
//            fund2.setName("common_fund");
//            fund2.setDescription("Quỹ dành cho chi tiêu hàng tháng");
//            fund2.setType(FundType.COMMON);
//            fund2.setAmount(new BigDecimal(30000));
//
//
//            fundRepository.saveAll(Arrays.asList(fund1, fund2));
//        };
//    }
}
