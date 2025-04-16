package com.huybq.fund_management.domain.event;

import com.huybq.fund_management.domain.user.User;
import com.huybq.fund_management.domain.user.UserResponseDTO;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@Data
public class EventResponeseDTO {
    private Long id;

    private String name;

    private LocalDateTime eventTime;

    private String location;

    private List<UserResponseDTO> hosts;

}
