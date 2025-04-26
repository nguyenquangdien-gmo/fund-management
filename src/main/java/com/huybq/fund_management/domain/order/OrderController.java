package com.huybq.fund_management.domain.order;

import com.huybq.fund_management.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
//    @GetMapping
//    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
//        List<OrderResponseDto> orders = orderService.getAllOrders();
//        return new ResponseEntity<>(orders, HttpStatus.OK);
//    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Lấy danh sách orders từ service, lọc theo startDate và endDate nếu có
        List<OrderResponseDto> orders = orderService.getOrdersByDateRange(startDate, endDate);

        // Trả về response
        return ResponseEntity.ok(orders);
    }
}

