package com.huybq.fund_management.domain.order;

import com.huybq.fund_management.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${server.version}/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Tạo đơn hàng mới
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestBody OrderRequestDto orderRequest,
            @AuthenticationPrincipal User user) {

        Long userId = user.getId();

        // Gọi service để tạo đơn hàng
        OrderResponseDto orderResponseDto = orderService.createOrder(userId, orderRequest);
        return new ResponseEntity<>(orderResponseDto, HttpStatus.CREATED);
    }

    // Lấy danh sách đơn hàng
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        List<OrderResponseDto> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }
}

