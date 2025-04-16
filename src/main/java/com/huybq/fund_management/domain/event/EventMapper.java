package com.huybq.fund_management.domain.event;

import com.huybq.fund_management.domain.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventMapper {
    private final UserMapper userMapper;

    public EventResponeseDTO toResponseDTO(Event event) {
        return EventResponeseDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .eventTime(event.getEventTime())
                .location(event.getLocation())
                .hosts(event.getHosts().stream().map(userMapper::toResponseDTO).toList())
                .build();
    }
}
