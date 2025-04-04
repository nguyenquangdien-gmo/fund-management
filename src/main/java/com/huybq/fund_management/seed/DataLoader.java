package com.huybq.fund_management.seed;

import com.huybq.fund_management.domain.balance.Balance;
import com.huybq.fund_management.domain.balance.BalanceRepository;
import com.huybq.fund_management.domain.fund.Fund;
import com.huybq.fund_management.domain.fund.FundRepository;
import com.huybq.fund_management.domain.fund.FundType;
import com.huybq.fund_management.domain.role.Role;
import com.huybq.fund_management.domain.role.RoleRepository;
import com.huybq.fund_management.domain.team.Team;
import com.huybq.fund_management.domain.team.TeamRepository;
import com.huybq.fund_management.domain.user.entity.Status;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initData(
            BalanceRepository balanceRepository,
            FundRepository fundRepository,
            RoleRepository roleRepository,
            TeamRepository teamRepository,
            UserRepository userRepository) {
        return args -> {
            createBalanceIfNotExists(balanceRepository, "common_fund");
            createBalanceIfNotExists(balanceRepository, "snack_fund");

            createFundIfNotExists(fundRepository, "Ăn vặt", "Quỹ dành cho ăn vặt", FundType.SNACK, new BigDecimal("120000"));
            createFundIfNotExists(fundRepository, "Chung", "Quỹ dành cho các hoạt động chung", FundType.COMMON, new BigDecimal("30000"));

            createRoleIfNotExists(roleRepository, "ADMIN");
            createRoleIfNotExists(roleRepository, "MEMBER");

            createTeamIfNotExists(teamRepository, "Java", "java", "mo66frnazir7uqq397h6wjhnrw", "gnuook57mfg7mgw61oxmece6ty");

            createAdminUserIfNotExists(userRepository, teamRepository, roleRepository);
        };
    }

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

    private void createRoleIfNotExists(RoleRepository repository, String name) {
        if (repository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            repository.save(role);
        }
    }

    private void createTeamIfNotExists(TeamRepository repository, String name, String slug, String channelId, String token) {
        if (repository.findBySlug(slug).isEmpty()) {
            Team team = new Team();
            team.setName(name);
            team.setSlug(slug);
            team.setChannelId(channelId);
            team.setToken(token);
            repository.save(team);
        }
    }

    private void createAdminUserIfNotExists(UserRepository repository, TeamRepository teamRepository, RoleRepository roleRepository) {
        String adminEmail = "admin@example.com";

        var role = roleRepository.findByName("ADMIN");
        if (repository.findByEmail(adminEmail).isEmpty() && role.isPresent()) {
            User admin = new User();
            admin.setId(1L);
            admin.setEmail(adminEmail);
            admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
            admin.setFullName("Admin");
            admin.setRole(role.get());
            admin.setStatus(Status.ACTIVE);
            admin.setPhone("0123456789");
            admin.setPosition("Administrator");
            admin.setDob(LocalDate.of(1990, 1, 1));
            admin.setJoinDate(LocalDate.now());
            admin.setUserToken(UUID.randomUUID().toString());
            admin.setCreatedAt(LocalDateTime.now());
            teamRepository.findBySlug("java").ifPresent(admin::setTeam);

            repository.save(admin);
        }
    }
}
