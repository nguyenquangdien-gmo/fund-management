package com.huybq.fund_management.domain.order_feedback;

import com.huybq.fund_management.domain.order_item.OrderItem;
import com.huybq.fund_management.domain.user.User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor_ = @Autowired)
public class OrderItemVoteMapper {

    // Chuyển từ OrderItemVoteRequestDTO sang OrderItemVote (entity)
    public static OrderItemVote toEntity(OrderItemVoteRequestDTO dto, OrderItem orderItem, User user) {
        return OrderItemVote.builder()
                .orderItem(orderItem)
                .user(user)
                .rating(dto.getRating())
                .note(dto.getNote())
                .build();
    }

    // Chuyển từ OrderItemVote (entity) sang OrderItemVoteResponseDTO
    public static OrderItemVoteResponseDTO toResponseDTO(OrderItemVote vote) {
        return OrderItemVoteResponseDTO.builder()
                .id(vote.getId())
                .orderItemId(vote.getOrderItem().getId())
                .userId(vote.getUser().getId())
                .rating(vote.getRating())
                .note(vote.getNote())
                .build();
    }
}

