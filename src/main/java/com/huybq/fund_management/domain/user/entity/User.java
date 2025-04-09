    package com.huybq.fund_management.domain.user.entity;

    import com.fasterxml.jackson.annotation.JsonIgnore;
    import com.huybq.fund_management.domain.reminder.Reminder;
    import com.huybq.fund_management.domain.role.Role;
    import com.huybq.fund_management.domain.team.Team;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.hibernate.annotations.CreationTimestamp;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.Collections;
    import java.util.List;

    @Entity
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class User implements UserDetails {
        @Id
        private Long id;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(nullable = false)
        @JsonIgnore
        private String password;

        @Column(nullable = false)
        private String fullName;

        @ManyToOne
        @JoinColumn(name = "role_id")
        private Role role;

        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        private Status status = Status.ACTIVE;

        private String phone;

        private String position;
        private LocalDate dob;
        private LocalDate joinDate;

        @ManyToOne
        @JoinColumn(name = "team_id")
        private Team team;

        @JsonIgnore
        @ManyToMany(mappedBy = "users")
        private List<Reminder> reminders= new ArrayList<>();

        private String userIdChat;
        private boolean isDelete = false;


        @CreationTimestamp
        private LocalDateTime createdAt;

        @JsonIgnore
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            if (role == null) {
                return Collections.emptyList();
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
        }


        @JsonIgnore
        @Override
        public String getUsername() {
            return email;
        }

        @JsonIgnore
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @JsonIgnore
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @JsonIgnore
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return status == Status.ACTIVE;
        }
    }
