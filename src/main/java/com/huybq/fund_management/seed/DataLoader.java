package com.huybq.fund_management.seed;

import com.huybq.fund_management.domain.balance.Balance;
import com.huybq.fund_management.domain.balance.BalanceRepository;
import com.huybq.fund_management.domain.fund.Fund;
import com.huybq.fund_management.domain.fund.FundRepository;
import com.huybq.fund_management.domain.fund.FundType;
import com.huybq.fund_management.domain.penalty.Penalty;
import com.huybq.fund_management.domain.penalty.PenaltyRepository;
import com.huybq.fund_management.domain.team.Team;
import com.huybq.fund_management.domain.team.TeamRepository;
import com.huybq.fund_management.domain.user.entity.Roles;
import com.huybq.fund_management.domain.user.entity.Status;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initData(
            BalanceRepository balanceRepository,
            FundRepository fundRepository,
            PenaltyRepository penaltyRepository,
            TeamRepository teamRepository, UserRepository userRepository) {
        return args -> {
            // 🏦 Balance (Quỹ tiền)
            createBalanceIfNotExists(balanceRepository, "common_fund");
            createBalanceIfNotExists(balanceRepository, "snack_fund");

            // 💰 Fund (Quỹ)
            createFundIfNotExists(fundRepository, "Ăn vặt", "Quỹ dành cho ăn vặt" ,FundType.SNACK,new BigDecimal("120000"));
            createFundIfNotExists(fundRepository, "Chung", "Quỹ dành cho các hoạt động chung", FundType.COMMON, new BigDecimal("30000"));

            // ⚖️ Penalty (Phạt)
            createPenaltyIfNotExists(penaltyRepository, "Đi trễ", "Phạt khi đi làm muộn", "late-check-in", new BigDecimal("50000"));

            // 🏢 Team (Nhóm)
            createTeamIfNotExists(teamRepository, "Java", "java");

            // 🧑‍🤝‍🧑 User (Người dùng)
            createAdminUserIfNotExists(userRepository, teamRepository);
        };
    }

    // --- HÀM HỖ TRỢ ---

    private void createBalanceIfNotExists(BalanceRepository repository, String title) {
        if (repository.findBalanceByTitle(title).isEmpty()) {
            Balance balance = new Balance();
            balance.setTitle(title);
            balance.setTotalAmount(BigDecimal.ZERO);
            repository.save(balance);
        }
    }

    private void createFundIfNotExists(FundRepository repository, String name, String description, FundType type, BigDecimal amount) {
        if (repository.findByName(name).isEmpty()) {
            Fund fund = new Fund();
            fund.setName(name);
            fund.setDescription(description);
            fund.setType(type);
            fund.setAmount(amount);
            fund.setCreatedAt(LocalDateTime.now());
            fund.setUpdatedAt(LocalDateTime.now());
            repository.save(fund);
        }
    }

    private void createPenaltyIfNotExists(PenaltyRepository repository, String name, String description, String slug, BigDecimal amount) {
        if (repository.findBySlug(slug).isEmpty()) {
            Penalty penalty = new Penalty();
            penalty.setName(name);
            penalty.setDescription(description);
            penalty.setSlug(slug);
            penalty.setAmount(amount);
            penalty.setCreatedAt(LocalDateTime.now());
            penalty.setUpdatedAt(LocalDateTime.now());
            repository.save(penalty);
        }
    }

    private void createTeamIfNotExists(TeamRepository repository, String name, String slug) {
        if (repository.findBySlug(slug).isEmpty()) {
            Team team = new Team();
            team.setName(name);
            team.setSlug(slug);
            repository.save(team);
        }
    }

    private void createAdminUserIfNotExists(UserRepository repository, TeamRepository teamRepository) {
        String adminEmail = "admin@runsystem.net";

        if (repository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setId(1L); // Nếu ID được tự động sinh, có thể bỏ dòng này
            admin.setEmail(adminEmail);
            admin.setPassword(new BCryptPasswordEncoder().encode("admin@123")); // Mật khẩu mã hóa
            admin.setFullName("Admin");
            admin.setRole(Roles.ADMIN);
            admin.setStatus(Status.ACTIVE);
            admin.setPhone("0123456789");
            admin.setPosition("Administrator");
            admin.setUserToken(UUID.randomUUID().toString());
            admin.setCreatedAt(LocalDateTime.now());

            // Gán team nếu có
            teamRepository.findBySlug("java").ifPresent(admin::setTeam);

            repository.save(admin);
        }
    }

}
