package com.huybq.fund_management.domain.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huybq.fund_management.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Team {
    @Id
    @GeneratedValue
    private String id;
    private String name;
    private String slug;

    @JsonIgnore
    @OneToMany(mappedBy = "team")
    private List<User> members;


}
