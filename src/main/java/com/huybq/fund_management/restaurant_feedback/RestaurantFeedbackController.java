package com.huybq.fund_management.restaurant_feedback;

import com.huybq.fund_management.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/${server.version}/feedback")
@RequiredArgsConstructor
public class RestaurantFeedbackController {

    private final RestaurantFeedbackService feedbackService;

    @PostMapping
    public void submitFeedback(
            @RequestBody RestaurantFeedbackRequestDto restaurantFeedback,
            @AuthenticationPrincipal User user // hoặc Jwt nếu dùng token
    ) {
        Long userId = user.getId(); // Lấy từ security context
        feedbackService.submitFeedback(userId, restaurantFeedback);
    }
}
