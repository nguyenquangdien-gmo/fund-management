package com.huybq.fund_management.domain.team;

import com.huybq.fund_management.domain.late.Late;
import com.huybq.fund_management.domain.user.entity.User;
import com.huybq.fund_management.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository repository;
    private final TeamMapper mapper;
    private final UserRepository userRepository;

    public List<TeamDTO> getMembers(String name) {
        return repository.findUsersByName(name).stream()
                .map(mapper::toDTO)
                .toList();
    }

}
