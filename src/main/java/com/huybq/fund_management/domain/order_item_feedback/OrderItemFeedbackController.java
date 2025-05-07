package com.huybq.fund_management.domain.order_item_feedback;

import com.huybq.fund_management.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${server.version}/order-item-feedback")
@RequiredArgsConstructor
public class OrderItemFeedbackController {

    private final OrderItemFeedbackService orderItemFeedbackService;

    @PostMapping
    public ResponseEntity<OrderItemFeedbackResponseDTO> createFeedback(
            @AuthenticationPrincipal User user,
            @RequestBody OrderItemFeedbackRequestDTO request) {
        return ResponseEntity.ok(orderItemFeedbackService.createFeedback(user, request));
    }

}