package com.huybq.fund_management.domain.order;

import com.huybq.fund_management.domain.order_item.OrderItemRequestDTO;
import com.huybq.fund_management.domain.order_item.OrderItemResponseDTO;
import com.huybq.fund_management.domain.order_item.OrderItemService;
import com.huybq.fund_management.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/${server.version}/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

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

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Lấy danh sách orders từ service, lọc theo startDate và endDate nếu có
        List<OrderResponseDto> orders = orderService.getOrdersByDateRange(startDate, endDate);

        // Trả về response
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        OrderResponseDto order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Xóa đơn hàng thành công");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/details")
    public ResponseEntity<List<OrderItemResponseDTO>> getItemsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderItemService.getItemsByOrder(orderId));
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<OrderItemResponseDTO> createItem(
            @RequestBody OrderItemRequestDTO orderItem,
            @AuthenticationPrincipal User user
    ) {
        OrderItemResponseDTO response = orderItemService.createItem(user.getId(), orderItem);
        return ResponseEntity.ok(response);
    }
}

