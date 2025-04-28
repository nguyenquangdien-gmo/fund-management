package com.huybq.fund_management.domain.order_feedback;

import com.huybq.fund_management.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/${server.version}/order-item-vote")
public class OrderItemVoteController {

    private final OrderItemVoteService orderItemVoteService;

    @PostMapping
    public ResponseEntity<OrderItemVoteResponseDTO> createVote(
            @AuthenticationPrincipal User user,
            @RequestBody OrderItemVoteRequestDTO request) {
        OrderItemVoteResponseDTO response = orderItemVoteService.createVote(user, request);
        return ResponseEntity.ok(response);
    }
}
